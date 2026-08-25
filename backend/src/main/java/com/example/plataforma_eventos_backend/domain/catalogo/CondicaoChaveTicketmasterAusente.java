package com.example.plataforma_eventos_backend.domain.catalogo;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Ativa CatalogoProviderLocal quando não há chave da Ticketmaster configurada — negação de
 * CondicaoChaveTicketmasterPresente, não @ConditionalOnProperty(havingValue = ""): esse
 * havingValue vazio faz StringUtils.hasLength("") retornar false, e o Spring cai no ramo
 * "propriedade presente e diferente de 'false'" — ou seja, a condição batia sempre que a
 * chave estivesse preenchida também, ligando os dois providers ao mesmo tempo.
 */
public class CondicaoChaveTicketmasterAusente implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String chave = context.getEnvironment().getProperty("ticketmaster.api.key");
        return chave == null || chave.isBlank();
    }
}
