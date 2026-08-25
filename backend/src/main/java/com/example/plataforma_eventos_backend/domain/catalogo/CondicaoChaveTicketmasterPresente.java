package com.example.plataforma_eventos_backend.domain.catalogo;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Ativa TicketmasterCatalogoProvider só quando a chave está de fato preenchida.
 * @ConditionalOnProperty sem havingValue trataria "" como presente (não é "false"), o que
 * ligaria os dois providers ao mesmo tempo quando a variável de ambiente não está setada —
 * daí a checagem manual de "não vazia" aqui em vez da anotação.
 */
public class CondicaoChaveTicketmasterPresente implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String chave = context.getEnvironment().getProperty("ticketmaster.api.key");
        return chave != null && !chave.isBlank();
    }
}
