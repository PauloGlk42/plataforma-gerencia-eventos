package com.example.plataforma_eventos_backend.domain.evento.dtos;

import com.example.plataforma_eventos_backend.domain.evento.Evento;

import java.time.OffsetDateTime;

/**
 * Lista enxuta pro operador escolher o portão: sem preço, sem ocupação — a portaria não
 * precisa desses dados, só de identificar o evento certo.
 */
public record EventoPortariaDTO(
        Long id,
        String titulo,
        OffsetDateTime inicio,
        String localNome,
        String cidade
) {
    public static EventoPortariaDTO de(Evento evento) {
        return new EventoPortariaDTO(evento.getId(), evento.getTitulo(), evento.getInicio(),
                evento.getLocalNome(), evento.getCidade());
    }
}
