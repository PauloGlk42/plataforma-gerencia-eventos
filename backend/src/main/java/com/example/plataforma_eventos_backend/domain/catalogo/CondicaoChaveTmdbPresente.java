package com.example.plataforma_eventos_backend.domain.catalogo;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Ativa TmdbCatalogoProvider só quando a chave está de fato preenchida — mesmo motivo de
 * CondicaoChaveTicketmasterPresente (não usar @ConditionalOnProperty aqui: com havingValue
 * vazio o Spring trata como "sem havingValue", que bate mesmo com a chave preenchida).
 */
public class CondicaoChaveTmdbPresente implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String chave = context.getEnvironment().getProperty("tmdb.api.key");
        return chave != null && !chave.isBlank();
    }
}
