package com.example.plataforma_eventos_backend.domain.catalogo;

import com.example.plataforma_eventos_backend.domain.evento.FonteCatalogo;
import com.example.plataforma_eventos_backend.domain.evento.TipoEvento;
import com.example.plataforma_eventos_backend.infra.exception.RegraNegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Catálogo real de filmes via TMDB (The Movie Database), ativo só quando TMDB_API_KEY está
 * preenchida (ver CondicaoChaveTmdbPresente) — sem chave, CatalogoProviderLocal assume
 * sozinho, igual ao par Ticketmaster/show.
 *
 * Com termo de busca usa /search/movie (o organizador está procurando um filme
 * específico); sem termo usa /discover/movie ordenado por popularidade, pra ter o que
 * mostrar na tela mesmo sem busca — mesmo papel que o "keyword" vazio cumpre na
 * Ticketmaster.
 *
 * Data e local do evento NÃO entram no mapeamento de propósito: quem define onde e quando
 * a sessão acontece é o organizador, na tela de criação — o TMDB só empresta título,
 * imagem e sinopse do item do catálogo, igual aos outros providers.
 */
@Component
@Conditional(CondicaoChaveTmdbPresente.class)
public class TmdbCatalogoProvider implements CatalogoProvider {

    private static final String URL_BASE = "https://api.themoviedb.org";
    private static final String URL_IMAGEM_BASE = "https://image.tmdb.org/t/p/w500";
    private static final int TAMANHO_PAGINA = 10;

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final RestClient restClient;

    public TmdbCatalogoProvider() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .baseUrl(URL_BASE)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public List<ItemCatalogo> buscar(String termo, TipoEvento tipo) {
        boolean comTermo = termo != null && !termo.isBlank();
        JsonNode resposta;
        try {
            resposta = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(comTermo ? "/3/search/movie" : "/3/discover/movie")
                                .queryParam("api_key", apiKey)
                                .queryParam("language", "pt-BR");
                        if (comTermo) {
                            uriBuilder.queryParam("query", termo);
                        } else {
                            uriBuilder.queryParam("sort_by", "popularity.desc");
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RuntimeException e) {
            throw new RegraNegocioException(
                    "Não foi possível buscar o catálogo de filmes agora. Tente novamente em instantes.", e);
        }

        JsonNode resultados = resposta == null ? null : resposta.path("results");
        if (resultados == null || !resultados.isArray()) {
            return List.of();
        }

        List<ItemCatalogo> itens = new ArrayList<>();
        int i = 0;
        for (JsonNode filme : resultados) {
            if (i++ >= TAMANHO_PAGINA) {
                break;
            }
            itens.add(new ItemCatalogo(
                    textoOuNull(filme.path("id")),
                    textoOuNull(filme.path("title")),
                    textoOuNull(filme.path("overview")),
                    imagem(filme),
                    TipoEvento.FILME,
                    FonteCatalogo.TMDB));
        }
        return itens;
    }

    private String imagem(JsonNode filme) {
        String caminho = textoOuNull(filme.path("poster_path"));
        if (caminho == null) {
            caminho = textoOuNull(filme.path("backdrop_path"));
        }
        return caminho == null ? null : URL_IMAGEM_BASE + caminho;
    }

    private String textoOuNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String texto = node.asText(null);
        return (texto == null || texto.isBlank()) ? null : texto;
    }
}
