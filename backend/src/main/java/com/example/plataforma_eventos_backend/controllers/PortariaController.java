package com.example.plataforma_eventos_backend.controllers;

import com.example.plataforma_eventos_backend.domain.evento.dtos.EventoPortariaDTO;
import com.example.plataforma_eventos_backend.domain.ingresso.dtos.ValidacaoRespostaDTO;
import com.example.plataforma_eventos_backend.domain.ingresso.dtos.ValidarIngressoDTO;
import com.example.plataforma_eventos_backend.domain.user.User;
import com.example.plataforma_eventos_backend.services.EventoService;
import com.example.plataforma_eventos_backend.services.IngressoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PortariaController {

    private final EventoService eventoService;
    private final IngressoService ingressoService;

    public PortariaController(EventoService eventoService, IngressoService ingressoService) {
        this.eventoService = eventoService;
        this.ingressoService = ingressoService;
    }

    @GetMapping("/portaria/eventos")
    public ResponseEntity<List<EventoPortariaDTO>> eventos() {
        return ResponseEntity.ok(eventoService.buscarPublicadosParaPortaria());
    }

    @PostMapping("/validacao")
    public ResponseEntity<ValidacaoRespostaDTO> validar(@RequestBody @Valid ValidarIngressoDTO dto,
                                                           @AuthenticationPrincipal User portaria) {
        return ResponseEntity.ok(ingressoService.validar(dto.codigo(), dto.eventoId(), portaria));
    }
}
