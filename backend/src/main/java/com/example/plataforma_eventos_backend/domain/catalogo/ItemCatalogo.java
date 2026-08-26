package com.example.plataforma_eventos_backend.domain.catalogo;

import com.example.plataforma_eventos_backend.domain.evento.FonteCatalogo;
import com.example.plataforma_eventos_backend.domain.evento.TipoEvento;

public record ItemCatalogo(
        String idExterno,
        String titulo,
        String sinopse,
        String imagemUrl,
        TipoEvento tipo,
        FonteCatalogo fonte
) {
}
