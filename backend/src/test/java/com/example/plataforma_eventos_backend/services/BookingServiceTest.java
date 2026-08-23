package com.example.plataforma_eventos_backend.services;

import com.example.plataforma_eventos_backend.domain.evento.Evento;
import com.example.plataforma_eventos_backend.domain.evento.FonteCatalogo;
import com.example.plataforma_eventos_backend.domain.evento.Setor;
import com.example.plataforma_eventos_backend.domain.evento.StatusEvento;
import com.example.plataforma_eventos_backend.domain.evento.TipoEvento;
import com.example.plataforma_eventos_backend.domain.pedido.StatusPedido;
import com.example.plataforma_eventos_backend.domain.pedido.dtos.CriarPedidoDTO;
import com.example.plataforma_eventos_backend.domain.pedido.dtos.ItemPedidoDTO;
import com.example.plataforma_eventos_backend.domain.pedido.dtos.PedidoDetalheDTO;
import com.example.plataforma_eventos_backend.domain.user.User;
import com.example.plataforma_eventos_backend.infra.exception.RegraNegocioException;
import com.example.plataforma_eventos_backend.repositories.EventoRepository;
import com.example.plataforma_eventos_backend.repositories.PedidoRepository;
import com.example.plataforma_eventos_backend.repositories.SetorRepository;
import com.example.plataforma_eventos_backend.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Roda contra o Postgres local (mesmo banco usado em dev, via application.properties) —
 * H2 não reproduz update condicional sob concorrência real, que é exatamente o que estes
 * testes verificam. Cada teste cria e limpa seu próprio evento/setor/pedido.
 */
@SpringBootTest
@TestPropertySource(properties = "spring.datasource.hikari.maximum-pool-size=20")
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private SetorRepository setorRepository;
    @Autowired
    private PedidoRepository pedidoRepository;
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
                .forEach(p -> pedidoRepository.deleteById(p.getId()));
        eventoRepository.deleteById(evento.getId());
    }

    @Test
    void concorrenciaNaoVendeAlemDaCapacidade() throws InterruptedException {
        criarEventoESetor(10);
        int totalThreads = 50;

        ExecutorService pool = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch largada = new CountDownLatch(1);
        CountDownLatch chegada = new CountDownLatch(totalThreads);
        AtomicInteger sucessos = new AtomicInteger();
        AtomicInteger semEstoque = new AtomicInteger();

        for (int i = 0; i < totalThreads; i++) {
            pool.submit(() -> {
                try {
                    largada.await();
                    bookingService.reservar(pedidoDeUmLugar(), cliente);
                    sucessos.incrementAndGet();
                } catch (RegraNegocioException e) {
                    semEstoque.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    chegada.countDown();
                }
            });
        }

        largada.countDown();
        assertEquals(true, chegada.await(30, TimeUnit.SECONDS), "threads não terminaram a tempo");
        pool.shutdown();

        Setor atual = setorRepository.findById(setor.getId()).orElseThrow();
        assertEquals(10, sucessos.get(), "exatamente a capacidade deveria ter sucesso");
        assertEquals(totalThreads - 10, semEstoque.get());
        assertEquals(10, atual.getOcupados());
        assertEquals(atual.getCapacidade(), atual.getOcupados());
    }

    @Test
    void cancelamentoDevolveExatamenteAQuantidadeReservada() {
        criarEventoESetor(20);

        PedidoDetalheDTO pedido = bookingService.reservar(
                new CriarPedidoDTO(evento.getId(), List.of(new ItemPedidoDTO(setor.getId(), 3))), cliente);

        assertEquals(3, setorRepository.findById(setor.getId()).orElseThrow().getOcupados());

        bookingService.cancelar(pedido.id(), cliente);

        Setor atual = setorRepository.findById(setor.getId()).orElseThrow();
        assertEquals(0, atual.getOcupados());
        assertEquals(StatusPedido.CANCELADO, pedidoRepository.findById(pedido.id()).orElseThrow().getStatus());
    }

    @Test
    void expiracaoAplicadaDuasVezesNaoDevolveEstoqueDuasVezes() throws InterruptedException {
        criarEventoESetor(20);

        PedidoDetalheDTO pedido = bookingService.reservar(
                new CriarPedidoDTO(evento.getId(), List.of(new ItemPedidoDTO(setor.getId(), 2))), cliente);
        assertEquals(2, setorRepository.findById(setor.getId()).orElseThrow().getOcupados());

        var pedidoEntity = pedidoRepository.findById(pedido.id()).orElseThrow();
        pedidoEntity.setExpiraEm(OffsetDateTime.now().minusMinutes(1));
        pedidoRepository.save(pedidoEntity);

        // duas "execuções do job" correndo juntas sobre o mesmo pedido vencido
        int execucoesParalelas = 2;
        ExecutorService pool = Executors.newFixedThreadPool(execucoesParalelas);
        CountDownLatch largada = new CountDownLatch(1);
        CountDownLatch chegada = new CountDownLatch(execucoesParalelas);

        for (int i = 0; i < execucoesParalelas; i++) {
            pool.submit(() -> {
                try {
                    largada.await();
                    bookingService.expirarVencidos();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    chegada.countDown();
                }
            });
        }

        largada.countDown();
        assertEquals(true, chegada.await(30, TimeUnit.SECONDS), "threads não terminaram a tempo");
        pool.shutdown();

        Setor atual = setorRepository.findById(setor.getId()).orElseThrow();
        assertEquals(0, atual.getOcupados(), "estoque não pode voltar duas vezes");
        assertEquals(StatusPedido.EXPIRADO, pedidoRepository.findById(pedido.id()).orElseThrow().getStatus());
    }

    private CriarPedidoDTO pedidoDeUmLugar() {
        return new CriarPedidoDTO(evento.getId(), List.of(new ItemPedidoDTO(setor.getId(), 1)));
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
