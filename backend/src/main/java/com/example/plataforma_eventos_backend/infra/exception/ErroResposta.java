package com.example.plataforma_eventos_backend.infra.exception;

public record ErroResposta(int status, String mensagem, String campo) {
    public ErroResposta(int status, String mensagem) {
        this(status, mensagem, null);
    }
}
