package com.example.plataforma_eventos_backend.domain.evento.dtos;

import com.example.plataforma_eventos_backend.domain.evento.FonteCatalogo;
import com.example.plataforma_eventos_backend.domain.evento.StatusEvento;
import com.example.plataforma_eventos_backend.domain.evento.TipoEvento;

import java.time.OffsetDateTime;
import java.util.List;

public record EventoDetalheDTO(
        Long id,
        TipoEvento tipo,
        FonteCatalogo fonte,
        String idExterno,
        String titulo,
        String sinopse,
        String imagemUrl,
        String metadados,
        String localNome,
        String cidade,
        String uf,
        OffsetDateTime inicio,
        OffsetDateTime fim,
        StatusEvento status,
        List<SetorDTO> setores
) {
}
