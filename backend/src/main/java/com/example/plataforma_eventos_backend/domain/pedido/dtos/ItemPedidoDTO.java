package com.example.plataforma_eventos_backend.domain.pedido.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemPedidoDTO(
        @NotNull Long setorId,
        @NotNull @Min(1) Integer quantidade
) {
}
