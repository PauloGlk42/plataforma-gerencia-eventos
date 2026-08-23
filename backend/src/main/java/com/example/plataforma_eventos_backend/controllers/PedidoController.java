package com.example.plataforma_eventos_backend.controllers;

import com.example.plataforma_eventos_backend.domain.pedido.dtos.CriarPedidoDTO;
import com.example.plataforma_eventos_backend.domain.pedido.dtos.PedidoDetalheDTO;
import com.example.plataforma_eventos_backend.domain.user.User;
import com.example.plataforma_eventos_backend.services.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PedidoController {

    private final BookingService bookingService;

    public PedidoController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/pedidos")
    public ResponseEntity<PedidoDetalheDTO> reservar(@RequestBody @Valid CriarPedidoDTO dto,
                                                        @AuthenticationPrincipal User cliente) {
        PedidoDetalheDTO pedido = bookingService.reservar(dto, cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @GetMapping("/pedidos/{id}")
    public ResponseEntity<PedidoDetalheDTO> buscar(@PathVariable Long id, @AuthenticationPrincipal User cliente) {
        return ResponseEntity.ok(bookingService.buscar(id, cliente));
    }

    @GetMapping("/meus-pedidos")
    public ResponseEntity<List<PedidoDetalheDTO>> meusPedidos(@AuthenticationPrincipal User cliente) {
        return ResponseEntity.ok(bookingService.buscarPorCliente(cliente));
    }

    @DeleteMapping("/pedidos/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, @AuthenticationPrincipal User cliente) {
        bookingService.cancelar(id, cliente);
        return ResponseEntity.noContent().build();
    }
}
