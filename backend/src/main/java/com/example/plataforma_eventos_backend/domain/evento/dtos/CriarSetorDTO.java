package com.example.plataforma_eventos_backend.domain.evento.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CriarSetorDTO(
        @NotBlank @Size(max = 40) String slug,
        @NotBlank @Size(max = 80) String nome,
        @NotNull @DecimalMin(value = "0.01") BigDecimal preco,
        @NotNull @Min(1) Integer capacidade
) {
}
