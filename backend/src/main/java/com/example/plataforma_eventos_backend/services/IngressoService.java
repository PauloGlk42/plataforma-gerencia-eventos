package com.example.plataforma_eventos_backend.services;

import com.example.plataforma_eventos_backend.domain.evento.Evento;
import com.example.plataforma_eventos_backend.domain.ingresso.Ingresso;
import com.example.plataforma_eventos_backend.domain.ingresso.StatusIngresso;
import com.example.plataforma_eventos_backend.domain.ingresso.dtos.IngressoDTO;
import com.example.plataforma_eventos_backend.domain.ingresso.dtos.IngressoPublicoDTO;
import com.example.plataforma_eventos_backend.domain.ingresso.dtos.IngressosPorEventoDTO;
import com.example.plataforma_eventos_backend.domain.ingresso.dtos.ValidacaoRespostaDTO;
import com.example.plataforma_eventos_backend.domain.pedido.Pedido;
import com.example.plataforma_eventos_backend.domain.pedido.PedidoItem;
import com.example.plataforma_eventos_backend.domain.user.User;
import com.example.plataforma_eventos_backend.infra.exception.RecursoNaoEncontradoException;
import com.example.plataforma_eventos_backend.repositories.IngressoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * codigo = <identificador aleatório>.<HMAC-SHA256 do identificador, em base64url>, assinado
 * com chave própria (INGRESSO_SECRET) — separada do JWT_SECRET, pra comprometer uma não
 * comprometer a outra. assinaturaValida() confere a assinatura antes de tocar o banco:
 * código forjado é rejeitado sem custo de I/O.
 */
@Service
public class IngressoService {

    private static final String ALGORITMO_HMAC = "HmacSHA256";

    private final IngressoRepository ingressoRepository;

    @Value("${ingresso.security.secret}")
    private String secret;

    public IngressoService(IngressoRepository ingressoRepository) {
        this.ingressoRepository = ingressoRepository;
    }

    @Transactional
    public void emitir(Pedido pedido, List<PedidoItem> itens) {
        OffsetDateTime agora = OffsetDateTime.now();
        List<Ingresso> ingressos = new ArrayList<>();
        for (PedidoItem item : itens) {
            for (int i = 0; i < item.getQuantidade(); i++) {
                String identificador = UUID.randomUUID().toString().replace("-", "");
                Ingresso ingresso = new Ingresso();
                ingresso.setPedido(pedido);
                ingresso.setSetor(item.getSetor());
                ingresso.setCodigo(identificador + "." + assinar(identificador));
                ingresso.setStatus(StatusIngresso.VALIDO);
                ingresso.setTokenPublico(UUID.randomUUID());
                ingresso.setCriadoEm(agora);
                ingressos.add(ingresso);
            }
        }
        ingressoRepository.saveAll(ingressos);
    }

    /**
     * Ordem crítica: 1) assinatura, sem tocar no banco — código forjado nem chega a
     * consultar o ingresso; 2) evento, antes de marcar como utilizado — um ingresso
     * legítimo apresentado no portão errado não pode ser queimado, senão o cliente perde
     * a entrada ao tentar de novo no portão certo; 3) só então o UPDATE condicional que
     * consome o ingresso. 0 linhas afetadas nesse UPDATE = outra leitura venceu a corrida.
     */
    @Transactional
    public ValidacaoRespostaDTO validar(String codigo, Long eventoId, User portaria) {
        if (!assinaturaValida(codigo)) {
            return ValidacaoRespostaDTO.invalido();
        }

        Ingresso ingresso = ingressoRepository.findByCodigo(codigo).orElse(null);
        if (ingresso == null || ingresso.getStatus() == StatusIngresso.CANCELADO) {
            return ValidacaoRespostaDTO.invalido();
        }

        Evento evento = ingresso.getPedido().getEvento();
        if (!evento.getId().equals(eventoId)) {
            return ValidacaoRespostaDTO.eventoErrado();
        }

        if (ingresso.getStatus() == StatusIngresso.UTILIZADO) {
            return ValidacaoRespostaDTO.jaUtilizado(ingresso.getValidadoEm());
        }

        int linhasAfetadas = ingressoRepository.marcarUtilizado(ingresso.getId(), portaria.getId());
        if (linhasAfetadas == 0) {
            Ingresso atual = ingressoRepository.findById(ingresso.getId()).orElseThrow();
            return ValidacaoRespostaDTO.jaUtilizado(atual.getValidadoEm());
        }

        return ValidacaoRespostaDTO.valido(evento.getTitulo(), ingresso.getSetor().getNome(), evento.getInicio());
    }

    public boolean assinaturaValida(String codigo) {
        int separador = codigo.lastIndexOf('.');
        if (separador <= 0 || separador == codigo.length() - 1) {
            return false;
        }
        String identificador = codigo.substring(0, separador);
        String assinaturaRecebida = codigo.substring(separador + 1);
        return MessageDigest.isEqual(
                assinar(identificador).getBytes(StandardCharsets.UTF_8),
                assinaturaRecebida.getBytes(StandardCharsets.UTF_8));
    }

    @Transactional
    public List<IngressosPorEventoDTO> buscarPorCliente(User cliente) {
        Map<Evento, List<Ingresso>> porEvento = new LinkedHashMap<>();
        for (Ingresso ingresso : ingressoRepository.findByCliente(cliente)) {
            porEvento.computeIfAbsent(ingresso.getPedido().getEvento(), e -> new ArrayList<>()).add(ingresso);
        }
        return porEvento.entrySet().stream()
                .map(entrada -> new IngressosPorEventoDTO(
                        entrada.getKey().getId(), entrada.getKey().getTitulo(), entrada.getKey().getInicio(),
                        entrada.getKey().getLocalNome(), entrada.getKey().getCidade(),
                        entrada.getValue().stream().map(IngressoDTO::de).toList()))
                .toList();
    }

    @Transactional
    public UUID compartilhar(Long ingressoId, User cliente) {
        Ingresso ingresso = ingressoRepository.findById(ingressoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ingresso não encontrado"));
        if (!ingresso.getPedido().getCliente().getId().equals(cliente.getId())) {
            throw new AccessDeniedException("Ingresso pertence a outro cliente");
        }
        return ingresso.getTokenPublico();
    }

    @Transactional
    public IngressoPublicoDTO buscarPublico(UUID token) {
        Ingresso ingresso = ingressoRepository.findByTokenPublico(token)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ingresso não encontrado"));
        Evento evento = ingresso.getPedido().getEvento();
        return new IngressoPublicoDTO(evento.getTitulo(), evento.getInicio(), evento.getLocalNome(),
                evento.getCidade(), ingresso.getSetor().getNome(), ingresso.getCodigo());
    }

    private String assinar(String identificador) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO_HMAC);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITMO_HMAC));
            byte[] assinatura = mac.doFinal(identificador.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(assinatura);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao assinar código do ingresso", e);
        }
    }
}
