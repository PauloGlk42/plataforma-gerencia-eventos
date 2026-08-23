package com.example.plataforma_eventos_backend.services;

import com.example.plataforma_eventos_backend.domain.evento.Evento;
import com.example.plataforma_eventos_backend.domain.evento.FonteCatalogo;
import com.example.plataforma_eventos_backend.domain.evento.Setor;
import com.example.plataforma_eventos_backend.domain.evento.StatusEvento;
import com.example.plataforma_eventos_backend.domain.evento.TipoEvento;
import com.example.plataforma_eventos_backend.domain.ingresso.Ingresso;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
