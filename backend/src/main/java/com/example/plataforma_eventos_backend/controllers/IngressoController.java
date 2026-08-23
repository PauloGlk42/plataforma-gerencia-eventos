package com.example.plataforma_eventos_backend.controllers;

import com.example.plataforma_eventos_backend.domain.ingresso.dtos.CompartilharIngressoRespostaDTO;
import com.example.plataforma_eventos_backend.domain.ingresso.dtos.IngressoPublicoDTO;
import com.example.plataforma_eventos_backend.domain.ingresso.dtos.IngressosPorEventoDTO;
import com.example.plataforma_eventos_backend.domain.user.User;
import com.example.plataforma_eventos_backend.services.IngressoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class IngressoController {

    private final IngressoService ingressoService;

    public IngressoController(IngressoService ingressoService) {
        this.ingressoService = ingressoService;
    }

    @GetMapping("/meus-ingressos")
    public ResponseEntity<List<IngressosPorEventoDTO>> meusIngressos(@AuthenticationPrincipal User cliente) {
        return ResponseEntity.ok(ingressoService.buscarPorCliente(cliente));
    }

    @PostMapping("/ingressos/{id}/compartilhar")
    public ResponseEntity<CompartilharIngressoRespostaDTO> compartilhar(@PathVariable Long id,
                                                                          @AuthenticationPrincipal User cliente) {
        UUID token = ingressoService.compartilhar(id, cliente);
        return ResponseEntity.ok(new CompartilharIngressoRespostaDTO(token));
    }

    @GetMapping("/publico/ingressos/{token}")
    public ResponseEntity<IngressoPublicoDTO> publico(@PathVariable UUID token) {
        return ResponseEntity.ok(ingressoService.buscarPublico(token));
    }
}
