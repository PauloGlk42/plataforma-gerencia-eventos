package com.example.plataforma_eventos_backend.domain.ingresso.dtos;

import com.example.plataforma_eventos_backend.domain.ingresso.ResultadoValidacao;

import java.time.OffsetDateTime;

/**
 * Campos de VALIDO são só o que o operador confere na tela (evento, setor, horário) —
 * nunca dado do comprador. JA_UTILIZADO só traz validadoEm; INVALIDO e EVENTO_ERRADO não
 * trazem detalhe nenhum, pra não vazar se o código pertence a outro evento real.
 */
public record ValidacaoRespostaDTO(
        ResultadoValidacao resultado,
        String eventoTitulo,
        String setorNome,
        OffsetDateTime eventoInicio,
        OffsetDateTime validadoEm
) {
    public static ValidacaoRespostaDTO valido(String eventoTitulo, String setorNome, OffsetDateTime eventoInicio) {
        return new ValidacaoRespostaDTO(ResultadoValidacao.VALIDO, eventoTitulo, setorNome, eventoInicio, null);
    }

    public static ValidacaoRespostaDTO invalido() {
        return new ValidacaoRespostaDTO(ResultadoValidacao.INVALIDO, null, null, null, null);
    }

    public static ValidacaoRespostaDTO jaUtilizado(OffsetDateTime validadoEm) {
        return new ValidacaoRespostaDTO(ResultadoValidacao.JA_UTILIZADO, null, null, null, validadoEm);
    }

    public static ValidacaoRespostaDTO eventoErrado() {
        return new ValidacaoRespostaDTO(ResultadoValidacao.EVENTO_ERRADO, null, null, null, null);
    }
}
