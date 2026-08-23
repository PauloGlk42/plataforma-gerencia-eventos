package com.example.plataforma_eventos_backend.services;

import com.example.plataforma_eventos_backend.domain.catalogo.CatalogoProvider;
import com.example.plataforma_eventos_backend.domain.catalogo.ItemCatalogo;
import com.example.plataforma_eventos_backend.domain.evento.TipoEvento;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogoService {

    private final CatalogoProvider catalogoProvider;

    public CatalogoService(CatalogoProvider catalogoProvider) {
        this.catalogoProvider = catalogoProvider;
    }

    @Cacheable(cacheNames = "catalogo",
            key = "(#termo == null ? '' : #termo.trim().toLowerCase()) + '|' + (#tipo == null ? '' : #tipo)")
    public List<ItemCatalogo> buscar(String termo, TipoEvento tipo) {
        return catalogoProvider.buscar(termo, tipo);
    }
}
