package com.example.plataforma_eventos_backend.domain.pagamento.dtos;

import com.example.plataforma_eventos_backend.domain.pagamento.StatusPagamento;
import com.example.plataforma_eventos_backend.domain.pedido.dtos.PedidoDetalheDTO;

public record PagamentoRespostaDTO(
        StatusPagamento status,
        String motivo,
        PedidoDetalheDTO pedido
) {
}
