package com.example.plataforma_eventos_backend.domain.ingresso.dtos;

import com.example.plataforma_eventos_backend.domain.ingresso.Ingresso;
import com.example.plataforma_eventos_backend.domain.ingresso.StatusIngresso;

import java.util.UUID;

public record IngressoDTO(
        Long id,
        String setorNome,
        String codigo,
        StatusIngresso status,
        UUID tokenPublico
) {
    public static IngressoDTO de(Ingresso ingresso) {
        return new IngressoDTO(ingresso.getId(), ingresso.getSetor().getNome(), ingresso.getCodigo(),
                ingresso.getStatus(), ingresso.getTokenPublico());
    }
}
