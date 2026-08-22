package com.example.plataforma_eventos_backend.domain.evento.dtos;

import com.example.plataforma_eventos_backend.domain.evento.StatusEvento;
import com.example.plataforma_eventos_backend.domain.evento.TipoEvento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EventoResumoDTO(
        Long id,
        TipoEvento tipo,
        String titulo,
        String imagemUrl,
        String localNome,
        String cidade,
        String uf,
        OffsetDateTime inicio,
        StatusEvento status,
        BigDecimal precoMinimo
) {
}
