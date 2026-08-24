package com.example.plataforma_eventos_backend.services;

import com.example.plataforma_eventos_backend.domain.evento.Evento;
import com.example.plataforma_eventos_backend.domain.evento.FonteCatalogo;
import com.example.plataforma_eventos_backend.domain.evento.Setor;
import com.example.plataforma_eventos_backend.domain.evento.StatusEvento;
import com.example.plataforma_eventos_backend.domain.evento.TipoEvento;
import com.example.plataforma_eventos_backend.domain.ingresso.Ingresso;
import com.example.plataforma_eventos_backend.domain.ingresso.ResultadoValidacao;
import com.example.plataforma_eventos_backend.domain.ingresso.StatusIngresso;
import com.example.plataforma_eventos_backend.domain.ingresso.dtos.ValidacaoRespostaDTO;
import com.example.plataforma_eventos_backend.domain.pedido.Pedido;
import com.example.plataforma_eventos_backend.domain.pedido.PedidoItem;
import com.example.plataforma_eventos_backend.domain.pedido.dtos.CriarPedidoDTO;
import com.example.plataforma_eventos_backend.domain.pedido.dtos.ItemPedidoDTO;
import com.example.plataforma_eventos_backend.domain.pedido.dtos.PedidoDetalheDTO;
import com.example.plataforma_eventos_backend.domain.user.User;
import com.example.plataforma_eventos_backend.repositories.EventoRepository;
import com.example.plataforma_eventos_backend.repositories.IngressoRepository;
import com.example.plataforma_eventos_backend.repositories.PedidoItemRepository;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class IngressoServiceTest {

    @Autowired
    private IngressoService ingressoService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private SetorRepository setorRepository;
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private PedidoItemRepository pedidoItemRepository;
    @Autowired
    private IngressoRepository ingressoRepository;
    @Autowired
    private UserRepository userRepository;

    private User organizador;
    private User cliente;
    private User portaria;
    private Evento evento;
    private Setor setor;

    @BeforeEach
    void setUp() {
        organizador = (User) userRepository.findByLogin("organizador@evento.com");
        cliente = (User) userRepository.findByLogin("cliente1@evento.com");
        portaria = (User) userRepository.findByLogin("portaria@evento.com");
        assertNotNull(organizador, "seed V8 (organizador@evento.com) precisa estar aplicado");
        assertNotNull(cliente, "seed V8 (cliente1@evento.com) precisa estar aplicado");
        assertNotNull(portaria, "seed V8 (portaria@evento.com) precisa estar aplicado");
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
                    pedidoRepository.deleteById(p.getId());
                });
        eventoRepository.deleteById(evento.getId());
    }

    @Test
    void codigoComAssinaturaAdulteradaEhRejeitadoPelaVerificacao() {
        criarEventoESetor(5);
        PedidoDetalheDTO pedidoDTO = bookingService.reservar(
                new CriarPedidoDTO(evento.getId(), List.of(new ItemPedidoDTO(setor.getId(), 1))), cliente);
        Pedido pedido = pedidoRepository.findById(pedidoDTO.id()).orElseThrow();
        List<PedidoItem> itens = pedidoItemRepository.findByPedido(pedido);

        ingressoService.emitir(pedido, itens);

        Ingresso emitido = ingressoRepository.findByPedido(pedido).get(0);
        String codigoValido = emitido.getCodigo();
        assertTrue(ingressoService.assinaturaValida(codigoValido), "código recém-emitido deveria ser válido");

        char ultimoChar = codigoValido.charAt(codigoValido.length() - 1);
        char trocado = ultimoChar == 'A' ? 'B' : 'A';
        String codigoAdulterado = codigoValido.substring(0, codigoValido.length() - 1) + trocado;
        assertFalse(ingressoService.assinaturaValida(codigoAdulterado), "assinatura adulterada não pode passar");

        assertFalse(ingressoService.assinaturaValida("identificador-forjado.assinatura-forjada"));
        assertFalse(ingressoService.assinaturaValida("sem-separador-de-assinatura"));
    }

    @Test
    void validaNaPrimeiraLeituraEJaUtilizadoNaSegunda() {
        criarEventoESetor(5);
        Ingresso ingresso = emitirIngressoDeTeste();

        ValidacaoRespostaDTO primeira = ingressoService.validar(ingresso.getCodigo(), evento.getId(), portaria);
        assertEquals(ResultadoValidacao.VALIDO, primeira.resultado());
        assertEquals(evento.getTitulo(), primeira.eventoTitulo());
        assertEquals(setor.getNome(), primeira.setorNome());

        ValidacaoRespostaDTO segunda = ingressoService.validar(ingresso.getCodigo(), evento.getId(), portaria);
        assertEquals(ResultadoValidacao.JA_UTILIZADO, segunda.resultado());
        assertNotNull(segunda.validadoEm());
    }

    @Test
    void eventoErradoDevolveEventoErradoESeguraIngressoValido() {
        criarEventoESetor(5);
        Ingresso ingresso = emitirIngressoDeTeste();
        long eventoInexistente = evento.getId() + 999_999L;

        ValidacaoRespostaDTO resposta = ingressoService.validar(ingresso.getCodigo(), eventoInexistente, portaria);
        assertEquals(ResultadoValidacao.EVENTO_ERRADO, resposta.resultado());

        Ingresso atual = ingressoRepository.findById(ingresso.getId()).orElseThrow();
        assertEquals(StatusIngresso.VALIDO, atual.getStatus());
        assertNull(atual.getValidadoEm());
    }

    @Test
    void assinaturaAdulteradaDevolveInvalido() {
        criarEventoESetor(5);

        ValidacaoRespostaDTO codigoForjado = ingressoService.validar(
                "identificador-forjado.assinatura-forjada", evento.getId(), portaria);
        assertEquals(ResultadoValidacao.INVALIDO, codigoForjado.resultado());

        ValidacaoRespostaDTO semSeparador = ingressoService.validar(
                "sem-separador-de-assinatura", evento.getId(), portaria);
        assertEquals(ResultadoValidacao.INVALIDO, semSeparador.resultado());
    }

    @Test
    void validacoesConcorrentesDoMesmoIngressoSoUmaRetornaValido() throws InterruptedException {
        criarEventoESetor(5);
        Ingresso ingresso = emitirIngressoDeTeste();
        int totalThreads = 10;

        ExecutorService pool = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch largada = new CountDownLatch(1);
        CountDownLatch chegada = new CountDownLatch(totalThreads);
        AtomicInteger validos = new AtomicInteger();
        AtomicInteger jaUtilizados = new AtomicInteger();

        for (int i = 0; i < totalThreads; i++) {
            pool.submit(() -> {
                try {
                    largada.await();
                    ValidacaoRespostaDTO resposta = ingressoService.validar(ingresso.getCodigo(), evento.getId(), portaria);
                    if (resposta.resultado() == ResultadoValidacao.VALIDO) {
                        validos.incrementAndGet();
                    } else if (resposta.resultado() == ResultadoValidacao.JA_UTILIZADO) {
                        jaUtilizados.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    chegada.countDown();
                }
            });
        }

        largada.countDown();
        assertTrue(chegada.await(30, TimeUnit.SECONDS), "threads não terminaram a tempo");
        pool.shutdown();

        assertEquals(1, validos.get(), "só uma validação concorrente pode ganhar");
        assertEquals(totalThreads - 1, jaUtilizados.get());
    }

    private Ingresso emitirIngressoDeTeste() {
        PedidoDetalheDTO pedidoDTO = bookingService.reservar(
                new CriarPedidoDTO(evento.getId(), List.of(new ItemPedidoDTO(setor.getId(), 1))), cliente);
        Pedido pedido = pedidoRepository.findById(pedidoDTO.id()).orElseThrow();
        List<PedidoItem> itens = pedidoItemRepository.findByPedido(pedido);
        ingressoService.emitir(pedido, itens);
        return ingressoRepository.findByPedido(pedido).get(0);
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
