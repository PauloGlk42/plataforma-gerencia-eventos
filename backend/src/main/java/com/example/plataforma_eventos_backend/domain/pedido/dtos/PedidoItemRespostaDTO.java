package com.example.plataforma_eventos_backend.domain.pedido.dtos;

import com.example.plataforma_eventos_backend.domain.pedido.PedidoItem;

import java.math.BigDecimal;

public record PedidoItemRespostaDTO(
        Long setorId,
        String setorNome,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {
    public static PedidoItemRespostaDTO de(PedidoItem item) {
        BigDecimal subtotal = item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
        return new PedidoItemRespostaDTO(item.getSetor().getId(), item.getSetor().getNome(),
                item.getQuantidade(), item.getPrecoUnitario(), subtotal);
    }
}
