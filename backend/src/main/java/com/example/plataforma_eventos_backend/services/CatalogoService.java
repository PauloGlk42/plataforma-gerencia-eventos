package com.example.plataforma_eventos_backend.services;

import com.example.plataforma_eventos_backend.domain.catalogo.CatalogoProviderLocal;
import com.example.plataforma_eventos_backend.domain.catalogo.ItemCatalogo;
import com.example.plataforma_eventos_backend.domain.catalogo.TicketmasterCatalogoProvider;
import com.example.plataforma_eventos_backend.domain.catalogo.TmdbCatalogoProvider;
import com.example.plataforma_eventos_backend.domain.evento.TipoEvento;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Show e filme resolvem sua fonte de forma independente: cada um usa o provider real
 * (Ticketmaster/TMDB) se a chave correspondente estiver configurada, senão cai pro
 * CatalogoProviderLocal só para aquele tipo — dá pra ter show real e filme local ao mesmo
 * tempo, ou vice-versa, sem um afetar o outro.
 */
@Service
public class CatalogoService {

    private final CatalogoProviderLocal local;
    private final Optional<TicketmasterCatalogoProvider> ticketmaster;
    private final Optional<TmdbCatalogoProvider> tmdb;

    public CatalogoService(CatalogoProviderLocal local,
                            Optional<TicketmasterCatalogoProvider> ticketmaster,
                            Optional<TmdbCatalogoProvider> tmdb) {
        this.local = local;
        this.ticketmaster = ticketmaster;
        this.tmdb = tmdb;
    }

    @Cacheable(cacheNames = "catalogo",
            key = "(#termo == null ? '' : #termo.trim().toLowerCase()) + '|' + (#tipo == null ? '' : #tipo)")
    public List<ItemCatalogo> buscar(String termo, TipoEvento tipo) {
        List<ItemCatalogo> itens = new ArrayList<>();
        if (tipo == null || tipo == TipoEvento.SHOW) {
            itens.addAll(ticketmaster.<List<ItemCatalogo>>map(provider -> provider.buscar(termo, TipoEvento.SHOW))
                    .orElseGet(() -> local.buscar(termo, TipoEvento.SHOW)));
        }
        if (tipo == null || tipo == TipoEvento.FILME) {
            itens.addAll(tmdb.<List<ItemCatalogo>>map(provider -> provider.buscar(termo, TipoEvento.FILME))
                    .orElseGet(() -> local.buscar(termo, TipoEvento.FILME)));
        }
        return itens;
    }
}
