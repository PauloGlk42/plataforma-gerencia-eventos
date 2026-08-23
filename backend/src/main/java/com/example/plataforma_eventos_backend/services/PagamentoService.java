package com.example.plataforma_eventos_backend.services;

import com.example.plataforma_eventos_backend.domain.pagamento.Pagamento;
import com.example.plataforma_eventos_backend.domain.pagamento.StatusPagamento;
import com.example.plataforma_eventos_backend.domain.pagamento.dtos.DadosCartaoDTO;
import com.example.plataforma_eventos_backend.domain.pagamento.dtos.PagamentoRespostaDTO;
import com.example.plataforma_eventos_backend.domain.pedido.Pedido;
import com.example.plataforma_eventos_backend.domain.pedido.PedidoItem;
import com.example.plataforma_eventos_backend.domain.pedido.StatusPedido;
import com.example.plataforma_eventos_backend.domain.user.User;
import com.example.plataforma_eventos_backend.infra.exception.RegraNegocioException;
import com.example.plataforma_eventos_backend.repositories.PagamentoRepository;
import com.example.plataforma_eventos_backend.repositories.PedidoItemRepository;
import com.example.plataforma_eventos_backend.repositories.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Simulação determinística por final do número do cartão (ver README): dá ao avaliador um
 * jeito reproduzível de exercitar aprovação e recusa, sem gateway real.
 *   final 0000 -> recusado por saldo insuficiente
 *   final 1111 -> recusado por suspeita de fraude
 *   qualquer outro -> aprovado
 * Recusa não altera o pedido (segue PENDENTE até expirar). Aprovação muda o pedido pra
 * PAGO e emite os ingressos na mesma transação, com a mesma transição condicional de
 * status usada pelo BookingService — pagar de novo um pedido já PAGO não emite duplicado.
 */
@Service
public class PagamentoService {

    private static final String FINAL_SALDO_INSUFICIENTE = "0000";
    private static final String FINAL_SUSPEITA_FRAUDE = "1111";

    private final PagamentoRepository pagamentoRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoItemRepository pedidoItemRepository;
    private final BookingService bookingService;
    private final IngressoService ingressoService;

    public PagamentoService(PagamentoRepository pagamentoRepository, PedidoRepository pedidoRepository,
                             PedidoItemRepository pedidoItemRepository, BookingService bookingService,
                             IngressoService ingressoService) {
        this.pagamentoRepository = pagamentoRepository;
        this.pedidoRepository = pedidoRepository;
        this.pedidoItemRepository = pedidoItemRepository;
        this.bookingService = bookingService;
        this.ingressoService = ingressoService;
    }

    @Transactional
    public PagamentoRespostaDTO pagar(Long pedidoId, DadosCartaoDTO dados, User cliente) {
        Pedido pedido = bookingService.obterEntidadeAtualizada(pedidoId, cliente);
        if (pedido.getStatus() != StatusPedido.PENDENTE) {
            throw new RegraNegocioException("Pedido não está mais pendente (vencido ou já processado)");
        }

        ResultadoSimulacao resultado = simular(dados.numero());

        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setStatus(resultado.status());
        pagamento.setMotivo(resultado.motivo());
        pagamento.setValor(pedido.getValorTotal());
        pagamento.setCriadoEm(OffsetDateTime.now());
        pagamentoRepository.save(pagamento);

        if (resultado.status() == StatusPagamento.RECUSADO) {
            return new PagamentoRespostaDTO(resultado.status(), resultado.motivo(),
                    bookingService.buscar(pedidoId, cliente));
        }

        int linhas = pedidoRepository.aprovarPagamento(pedidoId, StatusPedido.PENDENTE, StatusPedido.PAGO);
        if (linhas == 0) {
            throw new RegraNegocioException("Pedido já havia sido pago em outra requisição");
        }

        Pedido pedidoPago = pedidoRepository.findById(pedidoId).orElseThrow();
        List<PedidoItem> itens = pedidoItemRepository.findByPedido(pedidoPago);
        ingressoService.emitir(pedidoPago, itens);

        return new PagamentoRespostaDTO(resultado.status(), resultado.motivo(), bookingService.buscar(pedidoId, cliente));
    }

    private ResultadoSimulacao simular(String numeroCartao) {
        String finalCartao = numeroCartao.substring(numeroCartao.length() - 4);
        if (finalCartao.equals(FINAL_SALDO_INSUFICIENTE)) {
            return new ResultadoSimulacao(StatusPagamento.RECUSADO, "Saldo insuficiente (simulado)");
        }
        if (finalCartao.equals(FINAL_SUSPEITA_FRAUDE)) {
            return new ResultadoSimulacao(StatusPagamento.RECUSADO, "Suspeita de fraude (simulado)");
        }
        return new ResultadoSimulacao(StatusPagamento.APROVADO, null);
    }

    private record ResultadoSimulacao(StatusPagamento status, String motivo) {
    }
}
