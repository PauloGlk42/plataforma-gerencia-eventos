package com.example.plataforma_eventos_backend.domain.pagamento.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DadosCartaoDTO(
        @NotBlank @Pattern(regexp = "\\d{13,19}", message = "Número do cartão inválido") String numero,
        @NotBlank String nomeTitular,
        @NotBlank @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "Validade inválida (MM/AA)") String validade,
        @NotBlank @Pattern(regexp = "\\d{3,4}", message = "CVV inválido") String cvv
) {
}
