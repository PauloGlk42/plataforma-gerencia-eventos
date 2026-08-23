package com.example.plataforma_eventos_backend.domain.ingresso.dtos;

import java.time.OffsetDateTime;
import java.util.List;

public record IngressosPorEventoDTO(
        Long eventoId,
        String eventoTitulo,
        OffsetDateTime eventoInicio,
        String localNome,
        String cidade,
        List<IngressoDTO> ingressos
) {
}
