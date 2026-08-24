package com.example.plataforma_eventos_backend.domain.ingresso.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ValidarIngressoDTO(
        @NotBlank String codigo,
        @NotNull Long eventoId
) {
}
