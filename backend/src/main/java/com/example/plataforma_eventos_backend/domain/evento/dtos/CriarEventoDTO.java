package com.example.plataforma_eventos_backend.domain.evento.dtos;

import com.example.plataforma_eventos_backend.domain.evento.FonteCatalogo;
import com.example.plataforma_eventos_backend.domain.evento.TipoEvento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

public record CriarEventoDTO(
        @NotNull TipoEvento tipo,
        @NotNull FonteCatalogo fonte,
        String idExterno,
        @NotBlank @Size(max = 200) String titulo,
        String sinopse,
        @Size(max = 500) String imagemUrl,
        String metadados,
        @NotBlank @Size(max = 160) String localNome,
        @NotBlank @Size(max = 100) String cidade,
        @Size(max = 2) String uf,
        @NotNull OffsetDateTime inicio,
        OffsetDateTime fim,
        @NotEmpty @Valid List<CriarSetorDTO> setores
) {
}
