package com.example.plataforma_eventos_backend.domain.catalogo;

import com.example.plataforma_eventos_backend.domain.evento.TipoEvento;

import java.util.List;

public interface CatalogoProvider {
    List<ItemCatalogo> buscar(String termo, TipoEvento tipo);
}
