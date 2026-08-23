package com.example.plataforma_eventos_backend.domain.ingresso.dtos;

import java.time.OffsetDateTime;

// Sem nenhum dado do comprador: token_publico é a chave de acesso, não o codigo de validação.
public record IngressoPublicoDTO(
        String eventoTitulo,
        OffsetDateTime eventoInicio,
        String localNome,
        String cidade,
        String setorNome,
        String codigo
) {
}
