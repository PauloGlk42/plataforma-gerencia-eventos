package com.example.plataforma_eventos_backend.domain.pedido.dtos;

import com.example.plataforma_eventos_backend.domain.pedido.StatusPedido;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PedidoDetalheDTO(
        Long id,
        UUID codigo,
        Long eventoId,
        String eventoTitulo,
        StatusPedido status,
        BigDecimal valorTotal,
        OffsetDateTime expiraEm,
        OffsetDateTime criadoEm,
        List<PedidoItemRespostaDTO> itens
) {
}
