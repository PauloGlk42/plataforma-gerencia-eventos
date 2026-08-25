package com.example.plataforma_eventos_backend.domain.catalogo;

import com.example.plataforma_eventos_backend.domain.evento.FonteCatalogo;
import com.example.plataforma_eventos_backend.domain.evento.TipoEvento;
import com.example.plataforma_eventos_backend.infra.exception.RegraNegocioException;
import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Catálogo real via Ticketmaster Discovery API, ativo só quando TICKETMASTER_API_KEY está
 * preenchida (ver CondicaoChaveTicketmasterPresente) — sem chave, CatalogoProviderLocal
 * assume sozinho.
 *
 * Data e local do evento NÃO entram no mapeamento de propósito: quem define onde e quando
 * o evento acontece é o organizador, na tela de criação — a Ticketmaster só empresta
 * título, imagem e sinopse do item do catálogo, igual ao provider local.
 */
@Component
@Conditional(CondicaoChaveTicketmasterPresente.class)
public class TicketmasterCatalogoProvider implements CatalogoProvider {

    private static final String URL_BASE = "https://app.ticketmaster.com";
    private static final int TAMANHO_PAGINA = 10;
    // ordem de preferência de proporção de imagem — 16:9 é a mais próxima do que a tela de
    // criação de evento usa para os cards do catálogo
    private static final List<String> RATIOS_PREFERIDAS = List.of("16_9", "3_2", "4_3");

    @Value("${ticketmaster.api.key}")
    private String apiKey;

    private final RestClient restClient;

    public TicketmasterCatalogoProvider() {
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
        JsonNode resposta;
        try {
            resposta = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/discovery/v2/events.json")
                            .queryParam("keyword", termo == null ? "" : termo)
                            .queryParam("size", TAMANHO_PAGINA)
                            .queryParam("apikey", apiKey)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RuntimeException e) {
            throw new RegraNegocioException(
                    "Não foi possível buscar o catálogo da Ticketmaster agora. Tente novamente em instantes.", e);
        }

        JsonNode eventos = resposta == null ? null : resposta.path("_embedded").path("events");
        if (eventos == null || !eventos.isArray()) {
            return List.of();
        }

        List<ItemCatalogo> itens = new ArrayList<>();
        for (JsonNode evento : eventos) {
            itens.add(new ItemCatalogo(
                    textoOuNull(evento.path("id")),
                    textoOuNull(evento.path("name")),
                    sinopse(evento),
                    melhorImagem(evento.path("images")),
                    TipoEvento.SHOW,
                    FonteCatalogo.TICKETMASTER));
        }
        return itens;
    }

    private String sinopse(JsonNode evento) {
        String info = textoOuNull(evento.path("info"));
        if (info != null) {
            return info;
        }
        JsonNode classificacoes = evento.path("classifications");
        if (!classificacoes.isArray() || classificacoes.isEmpty()) {
            return null;
        }
        JsonNode classificacao = classificacoes.get(0);
        String segmento = textoOuNull(classificacao.path("segment").path("name"));
        String genero = textoOuNull(classificacao.path("genre").path("name"));
        if (genero != null && genero.equalsIgnoreCase("Undefined")) {
            genero = null;
        }
        if (segmento != null && genero != null) {
            return segmento + " — " + genero;
        }
        return segmento != null ? segmento : genero;
    }

    private String melhorImagem(JsonNode imagens) {
        if (!imagens.isArray()) {
            return null;
        }
        JsonNode melhor = null;
        int melhorPrioridade = Integer.MAX_VALUE;
        int melhorLargura = -1;
        for (JsonNode imagem : imagens) {
            String ratio = imagem.path("ratio").asText("");
            int prioridade = RATIOS_PREFERIDAS.indexOf(ratio);
            if (prioridade == -1) {
                prioridade = RATIOS_PREFERIDAS.size();
            }
            int largura = imagem.path("width").asInt(0);
            if (prioridade < melhorPrioridade || (prioridade == melhorPrioridade && largura > melhorLargura)) {
                melhor = imagem;
                melhorPrioridade = prioridade;
                melhorLargura = largura;
            }
        }
        return melhor == null ? null : textoOuNull(melhor.path("url"));
    }

    private String textoOuNull(JsonNode node) {
        return (node == null || node.isMissingNode() || node.isNull()) ? null : node.asText(null);
    }
}
