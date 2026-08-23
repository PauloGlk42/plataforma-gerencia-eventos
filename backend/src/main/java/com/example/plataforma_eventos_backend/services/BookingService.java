package com.example.plataforma_eventos_backend.services;

import com.example.plataforma_eventos_backend.domain.evento.Evento;
import com.example.plataforma_eventos_backend.domain.evento.Setor;
import com.example.plataforma_eventos_backend.domain.evento.StatusEvento;
import com.example.plataforma_eventos_backend.domain.pedido.Pedido;
import com.example.plataforma_eventos_backend.domain.pedido.PedidoItem;
import com.example.plataforma_eventos_backend.domain.pedido.StatusPedido;
import com.example.plataforma_eventos_backend.domain.pedido.dtos.CriarPedidoDTO;
import com.example.plataforma_eventos_backend.domain.pedido.dtos.ItemPedidoDTO;
import com.example.plataforma_eventos_backend.domain.pedido.dtos.PedidoDetalheDTO;
import com.example.plataforma_eventos_backend.domain.pedido.dtos.PedidoItemRespostaDTO;
import com.example.plataforma_eventos_backend.domain.user.User;
import com.example.plataforma_eventos_backend.infra.exception.RecursoNaoEncontradoException;
import com.example.plataforma_eventos_backend.infra.exception.RegraNegocioException;
import com.example.plataforma_eventos_backend.repositories.EventoRepository;
import com.example.plataforma_eventos_backend.repositories.PedidoItemRepository;
import com.example.plataforma_eventos_backend.repositories.PedidoRepository;
import com.example.plataforma_eventos_backend.repositories.SetorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Único ponto que altera setor.ocupados: reservar, cancelar e expirar entram todos por aqui.
 * Estoque é reservado já na criação do pedido (não no pagamento) e devolvido por update
 * condicional, sempre pelo mesmo método interno, pra manter a devolução idempotente.
 */
@Service
public class BookingService {

    private final PedidoRepository pedidoRepository;
    private final PedidoItemRepository pedidoItemRepository;
    private final SetorRepository setorRepository;
    private final EventoRepository eventoRepository;

    @Value("${app.pedido.limite-itens-por-setor:6}")
    private int limiteItensPorSetor;

    @Value("${app.pedido.expiracao-minutos:10}")
    private int expiracaoMinutos;

    public BookingService(PedidoRepository pedidoRepository, PedidoItemRepository pedidoItemRepository,
                           SetorRepository setorRepository, EventoRepository eventoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoItemRepository = pedidoItemRepository;
        this.setorRepository = setorRepository;
        this.eventoRepository = eventoRepository;
    }

    @Transactional
    public PedidoDetalheDTO reservar(CriarPedidoDTO dto, User cliente) {
        Evento evento = eventoRepository.findById(dto.eventoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        if (evento.getStatus() != StatusEvento.PUBLICADO) {
            throw new RegraNegocioException("Evento não está disponível para reserva");
        }

        // ordem determinística por setor_id: dois pedidos concorrentes tocando os mesmos
        // setores em ordens opostas não podem entrar em deadlock
        List<ItemPedidoDTO> itensOrdenados = dto.itens().stream()
                .sorted(Comparator.comparing(ItemPedidoDTO::setorId))
                .toList();
        validarSetoresUnicos(itensOrdenados);

        Pedido pedido = new Pedido();
        pedido.setCodigo(java.util.UUID.randomUUID());
        pedido.setCliente(cliente);
        pedido.setEvento(evento);
        pedido.setStatus(StatusPedido.PENDENTE);
        OffsetDateTime agora = OffsetDateTime.now();
        pedido.setExpiraEm(agora.plusMinutes(expiracaoMinutos));
        pedido.setCriadoEm(agora);
        pedido.setAtualizadoEm(agora);
        pedido.setValorTotal(BigDecimal.ZERO);
        pedidoRepository.save(pedido);

        List<PedidoItem> itens = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedidoDTO itemDto : itensOrdenados) {
            if (itemDto.quantidade() > limiteItensPorSetor) {
                throw new RegraNegocioException(
                        "Limite de " + limiteItensPorSetor + " ingressos por setor neste pedido");
            }
            Setor setor = setorRepository.findById(itemDto.setorId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Setor não encontrado"));
            if (!setor.getEvento().getId().equals(evento.getId())) {
                throw new RegraNegocioException("Setor não pertence ao evento informado");
            }

            int linhasAfetadas = setorRepository.incrementarOcupados(setor.getId(), itemDto.quantidade());
            if (linhasAfetadas == 0) {
                int restantes = restantesNoSetor(setor.getId());
                throw new RegraNegocioException("Restam apenas " + restantes + " lugares em " + setor.getNome());
            }

            PedidoItem item = new PedidoItem();
            item.setPedido(pedido);
            item.setSetor(setor);
            item.setQuantidade(itemDto.quantidade());
            item.setPrecoUnitario(setor.getPreco());
            itens.add(item);
            total = total.add(setor.getPreco().multiply(BigDecimal.valueOf(itemDto.quantidade())));
        }
        pedidoItemRepository.saveAll(itens);

        // update em massa acima limpou o contexto de persistência (clearAutomatically),
        // então "pedido" está detached; save() aqui faz merge e grava o valor_total real
        pedido.setValorTotal(total);
        pedidoRepository.save(pedido);

        return paraDetalhe(pedido, itens);
    }

    @Transactional
    public PedidoDetalheDTO buscar(Long pedidoId, User cliente) {
        Pedido pedido = expirarSeVencido(buscarDoCliente(pedidoId, cliente));
        return paraDetalhe(pedido, pedidoItemRepository.findByPedido(pedido));
    }

    @Transactional
    public List<PedidoDetalheDTO> buscarPorCliente(User cliente) {
        return pedidoRepository.findByCliente(cliente).stream()
                .map(this::expirarSeVencido)
                .map(pedido -> paraDetalhe(pedido, pedidoItemRepository.findByPedido(pedido)))
                .toList();
    }

    @Transactional
    public void cancelar(Long pedidoId, User cliente) {
        Pedido pedido = buscarDoCliente(pedidoId, cliente);
        if (!encerrarEDevolverEstoque(pedido.getId(), StatusPedido.CANCELADO)) {
            throw new RegraNegocioException("Pedido não está mais pendente");
        }
    }

    @Transactional
    public void expirarVencidos() {
        for (Long id : pedidoRepository.buscarIdsPendentesVencidos(StatusPedido.PENDENTE, OffsetDateTime.now())) {
            encerrarEDevolverEstoque(id, StatusPedido.EXPIRADO);
        }
    }

    /**
     * Muda o status por update condicional (WHERE status = PENDENTE) e só devolve estoque
     * se a transição afetou 1 linha. Chamado tanto pelo cancelamento manual quanto pelo job
     * de expiração — via única pra devolução, então rodar duas vezes sobre o mesmo pedido
     * nunca devolve o estoque em dobro.
     */
    private boolean encerrarEDevolverEstoque(Long pedidoId, StatusPedido novoStatus) {
        int linhas = pedidoRepository.atualizarStatusSe(pedidoId, StatusPedido.PENDENTE, novoStatus);
        if (linhas == 0) {
            return false;
        }
        Pedido referencia = pedidoRepository.getReferenceById(pedidoId);
        pedidoItemRepository.findByPedido(referencia).stream()
                .sorted(Comparator.comparing(item -> item.getSetor().getId()))
                .forEach(item -> setorRepository.decrementarOcupados(item.getSetor().getId(), item.getQuantidade()));
        return true;
    }

    /**
     * Checagem preguiçosa: pedido pendente vencido consultado por GET expira na hora,
     * sem depender do job (@Scheduled a cada 30s) já ter rodado.
     */
    private Pedido expirarSeVencido(Pedido pedido) {
        if (pedido.getStatus() == StatusPedido.PENDENTE && pedido.getExpiraEm() != null
                && pedido.getExpiraEm().isBefore(OffsetDateTime.now())) {
            encerrarEDevolverEstoque(pedido.getId(), StatusPedido.EXPIRADO);
            return pedidoRepository.findById(pedido.getId()).orElseThrow();
        }
        return pedido;
    }

    private Pedido buscarDoCliente(Long pedidoId, User cliente) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));
        if (!pedido.getCliente().getId().equals(cliente.getId())) {
            throw new AccessDeniedException("Pedido pertence a outro cliente");
        }
        return pedido;
    }

    private int restantesNoSetor(Long setorId) {
        Setor setor = setorRepository.findById(setorId).orElseThrow();
        return setor.getCapacidade() - setor.getOcupados();
    }

    private void validarSetoresUnicos(List<ItemPedidoDTO> itens) {
        long distintos = itens.stream().map(ItemPedidoDTO::setorId).distinct().count();
        if (distintos != itens.size()) {
            throw new RegraNegocioException("Não é possível repetir o mesmo setor no pedido");
        }
    }

    private PedidoDetalheDTO paraDetalhe(Pedido pedido, List<PedidoItem> itens) {
        return new PedidoDetalheDTO(pedido.getId(), pedido.getCodigo(), pedido.getEvento().getId(),
                pedido.getEvento().getTitulo(), pedido.getStatus(), pedido.getValorTotal(), pedido.getExpiraEm(),
                pedido.getCriadoEm(), itens.stream().map(PedidoItemRespostaDTO::de).toList());
    }
}
