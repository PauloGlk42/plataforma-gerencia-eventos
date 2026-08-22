package com.example.plataforma_eventos_backend.controllers;

import com.example.plataforma_eventos_backend.domain.catalogo.ItemCatalogo;
import com.example.plataforma_eventos_backend.domain.evento.TipoEvento;
import com.example.plataforma_eventos_backend.domain.evento.dtos.CriarEventoDTO;
import com.example.plataforma_eventos_backend.domain.evento.dtos.EventoDetalheDTO;
import com.example.plataforma_eventos_backend.domain.evento.dtos.EventoResumoDTO;
import com.example.plataforma_eventos_backend.domain.user.User;
import com.example.plataforma_eventos_backend.services.CatalogoService;
import com.example.plataforma_eventos_backend.services.EventoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class EventoController {

    private final EventoService eventoService;
    private final CatalogoService catalogoService;

    public EventoController(EventoService eventoService, CatalogoService catalogoService) {
        this.eventoService = eventoService;
        this.catalogoService = catalogoService;
    }

    @GetMapping("/catalogo")
    public ResponseEntity<List<ItemCatalogo>> buscarCatalogo(@RequestParam(required = false) String q,
                                                               @RequestParam(required = false) TipoEvento tipo) {
        return ResponseEntity.ok(catalogoService.buscar(q, tipo));
    }

    @PostMapping("/eventos")
    public ResponseEntity<EventoDetalheDTO> criar(@RequestBody @Valid CriarEventoDTO dto,
                                                    @AuthenticationPrincipal User organizador) {
        EventoDetalheDTO evento = eventoService.criar(dto, organizador);
        return ResponseEntity.status(HttpStatus.CREATED).body(evento);
    }

    @GetMapping("/eventos/meus")
    public ResponseEntity<List<EventoResumoDTO>> meusEventos(@AuthenticationPrincipal User organizador) {
        return ResponseEntity.ok(eventoService.buscarPorOrganizador(organizador));
    }

    @PostMapping("/eventos/{id}/publicar")
    public ResponseEntity<Void> publicar(@PathVariable Long id, @AuthenticationPrincipal User organizador) {
        eventoService.publicar(id, organizador);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/eventos")
    public ResponseEntity<Page<EventoResumoDTO>> listarPublicados(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime ate,
            @RequestParam(required = false) BigDecimal precoMin,
            @RequestParam(required = false) BigDecimal precoMax,
            @RequestParam(required = false) TipoEvento tipo,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(eventoService.buscarPublicados(q, cidade, tipo, de, ate, precoMin, precoMax, pageable));
    }

    @GetMapping("/eventos/{id}")
    public ResponseEntity<EventoDetalheDTO> detalhe(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.buscarDetalhePublico(id));
    }
}
