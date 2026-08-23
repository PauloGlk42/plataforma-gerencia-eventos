package com.example.plataforma_eventos_backend.services;

import com.example.plataforma_eventos_backend.domain.evento.Evento;
import com.example.plataforma_eventos_backend.domain.evento.FonteCatalogo;
import com.example.plataforma_eventos_backend.domain.evento.Setor;
import com.example.plataforma_eventos_backend.domain.evento.StatusEvento;
import com.example.plataforma_eventos_backend.domain.evento.TipoEvento;
import com.example.plataforma_eventos_backend.domain.ingresso.Ingresso;
import com.example.plataforma_eventos_backend.domain.ingresso.StatusIngresso;
import com.example.plataforma_eventos_backend.domain.pagamento.StatusPagamento;
import com.example.plataforma_eventos_backend.domain.pagamento.dtos.DadosCartaoDTO;
import com.example.plataforma_eventos_backend.domain.pagamento.dtos.PagamentoRespostaDTO;
import com.example.plataforma_eventos_backend.domain.pedido.Pedido;
import com.example.plataforma_eventos_backend.domain.pedido.StatusPedido;
import com.example.plataforma_eventos_backend.domain.pedido.dtos.CriarPedidoDTO;
import com.example.plataforma_eventos_backend.domain.pedido.dtos.ItemPedidoDTO;
import com.example.plataforma_eventos_backend.domain.pedido.dtos.PedidoDetalheDTO;
import com.example.plataforma_eventos_backend.domain.user.User;
import com.example.plataforma_eventos_backend.infra.exception.RegraNegocioException;
import com.example.plataforma_eventos_backend.repositories.EventoRepository;
import com.example.plataforma_eventos_backend.repositories.IngressoRepository;
import com.example.plataforma_eventos_backend.repositories.PagamentoRepository;
import com.example.plataforma_eventos_backend.repositories.PedidoRepository;
import com.example.plataforma_eventos_backend.repositories.SetorRepository;
import com.example.plataforma_eventos_backend.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Roda contra o Postgres local, igual BookingServiceTest — a garantia de "não emite
 * ingresso duplicado" depende da transição condicional real de status do pedido.
 */
@SpringBootTest
class PagamentoServiceTest {

    @Autowired
    private PagamentoService pagamentoService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private SetorRepository setorRepository;
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private IngressoRepository ingressoRepository;
    @Autowired
    private PagamentoRepository pagamentoRepository;
    @Autowired
    private UserRepository userRepository;

    private User organizador;
    private User cliente;
    private Evento evento;
    private Setor setor;

    @BeforeEach
    void setUp() {
        organizador = (User) userRepository.findByLogin("organizador@evento.com");
        cliente = (User) userRepository.findByLogin("cliente1@evento.com");
        assertNotNull(organizador, "seed V8 (organizador@evento.com) precisa estar aplicado");
        assertNotNull(cliente, "seed V8 (cliente1@evento.com) precisa estar aplicado");
    }

    @AfterEach
    void tearDown() {
        if (evento == null) {
            return;
        }
        pedidoRepository.findByCliente(cliente).stream()
                .filter(p -> p.getEvento().getId().equals(evento.getId()))
                .forEach(p -> {
                    ingressoRepository.findByPedido(p).forEach(i -> ingressoRepository.deleteById(i.getId()));
                    pagamentoRepository.findByPedido(p).forEach(pg -> pagamentoRepository.deleteById(pg.getId()));
                    pedidoRepository.deleteById(p.getId());
                });
        eventoRepository.deleteById(evento.getId());
    }

    @Test
    void pagamentoAprovadoEmiteQuantidadeCompradaComCodigosUnicos() {
        criarEventoESetor(20);
        PedidoDetalheDTO pedido = reservar(3);

        PagamentoRespostaDTO resposta = pagamentoService.pagar(pedido.id(), cartao("4111111111112222"), cliente);

        assertEquals(StatusPagamento.APROVADO, resposta.status());
        assertEquals(StatusPedido.PAGO, resposta.pedido().status());

        List<Ingresso> ingressos = ingressoRepository.findByPedido(pedidoRepository.findById(pedido.id()).orElseThrow());
        assertEquals(3, ingressos.size());
        assertEquals(3, ingressos.stream().map(Ingresso::getCodigo).distinct().count());
        assertEquals(3, ingressos.stream().map(Ingresso::getTokenPublico).distinct().count());
        ingressos.forEach(i -> assertEquals(StatusIngresso.VALIDO, i.getStatus()));
    }

    @Test
    void pagamentoRecusadoNaoEmiteIngressoNemAlteraEstoque() {
        criarEventoESetor(20);
        PedidoDetalheDTO pedido = reservar(2);

        PagamentoRespostaDTO resposta = pagamentoService.pagar(pedido.id(), cartao("4111111111110000"), cliente);

        assertEquals(StatusPagamento.RECUSADO, resposta.status());
        assertEquals(StatusPedido.PENDENTE, resposta.pedido().status());

        Pedido pedidoAtual = pedidoRepository.findById(pedido.id()).orElseThrow();
        assertEquals(StatusPedido.PENDENTE, pedidoAtual.getStatus());
        assertEquals(2, setorRepository.findById(setor.getId()).orElseThrow().getOcupados());
        assertEquals(0, ingressoRepository.findByPedido(pedidoAtual).size());
    }

    @Test
    void pagarPedidoJaPagoNaoEmiteIngressosDuplicados() {
        criarEventoESetor(20);
        PedidoDetalheDTO pedido = reservar(2);

        pagamentoService.pagar(pedido.id(), cartao("4111111111112222"), cliente);
        assertThrows(RegraNegocioException.class,
                () -> pagamentoService.pagar(pedido.id(), cartao("4111111111113333"), cliente));

        List<Ingresso> ingressos = ingressoRepository.findByPedido(pedidoRepository.findById(pedido.id()).orElseThrow());
        assertEquals(2, ingressos.size());
    }

    private PedidoDetalheDTO reservar(int quantidade) {
        return bookingService.reservar(
                new CriarPedidoDTO(evento.getId(), List.of(new ItemPedidoDTO(setor.getId(), quantidade))), cliente);
    }

    private DadosCartaoDTO cartao(String numero) {
        return new DadosCartaoDTO(numero, "Cliente Teste", "12/30", "123");
    }

    private void criarEventoESetor(int capacidade) {
        evento = new Evento();
        evento.setOrganizador(organizador);
        evento.setTipo(TipoEvento.SHOW);
        evento.setFonte(FonteCatalogo.LOCAL);
        evento.setTitulo("Evento de teste — " + UUID.randomUUID());
        evento.setLocalNome("Local de teste");
        evento.setCidade("Curitiba");
        evento.setUf("PR");
        evento.setInicio(OffsetDateTime.now().plusDays(30));
        evento.setStatus(StatusEvento.PUBLICADO);
        OffsetDateTime agora = OffsetDateTime.now();
        evento.setCriadoEm(agora);
        evento.setAtualizadoEm(agora);
        eventoRepository.save(evento);

        setor = new Setor();
        setor.setEvento(evento);
        setor.setSlug("PISTA");
        setor.setNome("Pista");
        setor.setPreco(new BigDecimal("50.00"));
        setor.setCapacidade(capacidade);
        setor.setOcupados(0);
        setorRepository.save(setor);
    }
}
