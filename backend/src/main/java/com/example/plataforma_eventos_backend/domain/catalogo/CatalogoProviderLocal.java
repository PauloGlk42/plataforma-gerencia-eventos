package com.example.plataforma_eventos_backend.domain.catalogo;

import com.example.plataforma_eventos_backend.domain.evento.TipoEvento;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Catálogo fixo usado quando não há chave da Ticketmaster configurada, para que o fluxo
 * de criação de evento funcione sem depender de rede ou de cadastro em API externa.
 */
@Component
@ConditionalOnProperty(name = "ticketmaster.api.key", havingValue = "", matchIfMissing = true)
public class CatalogoProviderLocal implements CatalogoProvider {

    private static final List<ItemCatalogo> ITENS = List.of(
            new ItemCatalogo("local-metallica-m72", "Metallica — M72 World Tour",
                    "Show de metal em São Paulo.", "https://picsum.photos/seed/metallica/600/400", TipoEvento.SHOW),
            new ItemCatalogo("local-iron-maiden-legacy", "Iron Maiden — Legacy of the Beast",
                    "Turnê clássica da banda britânica.", "https://picsum.photos/seed/iron-maiden/600/400", TipoEvento.SHOW),
            new ItemCatalogo("local-coldplay-music-of-the-spheres", "Coldplay — Music of the Spheres",
                    "Turnê com produção visual em grande escala.", "https://picsum.photos/seed/coldplay/600/400", TipoEvento.SHOW),
            new ItemCatalogo("local-taylor-swift-eras-tour", "Taylor Swift — The Eras Tour",
                    "Show percorrendo as eras da carreira da artista.", "https://picsum.photos/seed/taylor-swift/600/400", TipoEvento.SHOW),
            new ItemCatalogo("local-foo-fighters-everything-or-nothing", "Foo Fighters — Everything or Nothing Tour",
                    "Rock ao vivo com a banda de Dave Grohl.", "https://picsum.photos/seed/foo-fighters/600/400", TipoEvento.SHOW),
            new ItemCatalogo("local-dua-lipa-radical-optimism", "Dua Lipa — Radical Optimism Tour",
                    "Turnê do álbum Radical Optimism.", "https://picsum.photos/seed/dua-lipa/600/400", TipoEvento.SHOW),
            new ItemCatalogo("local-guns-n-roses-world-tour", "Guns N' Roses — World Tour",
                    "Clássicos do rock com a formação atual da banda.", "https://picsum.photos/seed/guns-n-roses/600/400", TipoEvento.SHOW),
            new ItemCatalogo("local-billie-eilish-hit-me-hard-and-soft", "Billie Eilish — Hit Me Hard and Soft Tour",
                    "Turnê do álbum Hit Me Hard and Soft.", "https://picsum.photos/seed/billie-eilish/600/400", TipoEvento.SHOW),
            new ItemCatalogo("local-duna-parte-tres", "Duna: Parte Três",
                    "Sessão de cinema — ficção científica.", "https://picsum.photos/seed/duna/600/400", TipoEvento.FILME),
            new ItemCatalogo("local-vingadores-doomsday", "Vingadores: Doomsday",
                    "Sessão de cinema — super-heróis.", "https://picsum.photos/seed/vingadores-doomsday/600/400", TipoEvento.FILME),
            new ItemCatalogo("local-missao-impossivel-acerto-de-contas", "Missão Impossível: Acerto de Contas",
                    "Sessão de cinema — ação.", "https://picsum.photos/seed/missao-impossivel/600/400", TipoEvento.FILME),
            new ItemCatalogo("local-divertida-mente-3", "Divertida Mente 3",
                    "Sessão de cinema — animação.", "https://picsum.photos/seed/divertida-mente-3/600/400", TipoEvento.FILME),
            new ItemCatalogo("local-coringa-delirio-a-dois", "Coringa: Delírio a Dois",
                    "Sessão de cinema — drama.", "https://picsum.photos/seed/coringa-2/600/400", TipoEvento.FILME),
            new ItemCatalogo("local-godzilla-e-kong-o-novo-imperio", "Godzilla e Kong: O Novo Império",
                    "Sessão de cinema — ficção científica.", "https://picsum.photos/seed/godzilla-kong/600/400", TipoEvento.FILME),
            new ItemCatalogo("local-homem-aranha-alem-do-aranhaverso", "Homem-Aranha: Além do Aranhaverso",
                    "Sessão de cinema — animação.", "https://picsum.photos/seed/homem-aranha/600/400", TipoEvento.FILME)
    );

    @Override
    public List<ItemCatalogo> buscar(String termo, TipoEvento tipo) {
        String termoNormalizado = termo == null ? null : termo.trim().toLowerCase();
        return ITENS.stream()
                .filter(item -> tipo == null || item.tipo() == tipo)
                .filter(item -> termoNormalizado == null || termoNormalizado.isBlank()
                        || item.titulo().toLowerCase().contains(termoNormalizado))
                .toList();
    }
}
