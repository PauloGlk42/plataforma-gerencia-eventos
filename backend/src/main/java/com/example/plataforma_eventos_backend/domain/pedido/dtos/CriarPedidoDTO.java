package com.example.plataforma_eventos_backend.domain.pedido.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CriarPedidoDTO(
        @NotNull Long eventoId,
        @NotEmpty @Valid List<ItemPedidoDTO> itens
) {
}
