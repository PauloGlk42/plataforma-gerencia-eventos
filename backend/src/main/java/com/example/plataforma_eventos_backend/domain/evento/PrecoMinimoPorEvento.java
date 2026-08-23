package com.example.plataforma_eventos_backend.domain.evento;

import java.math.BigDecimal;

public interface PrecoMinimoPorEvento {
    Long getEventoId();
    BigDecimal getPrecoMinimo();
}
