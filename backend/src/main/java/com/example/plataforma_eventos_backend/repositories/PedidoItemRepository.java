package com.example.plataforma_eventos_backend.repositories;

import com.example.plataforma_eventos_backend.domain.pedido.Pedido;
import com.example.plataforma_eventos_backend.domain.pedido.PedidoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoItemRepository extends JpaRepository<PedidoItem, Long> {
    List<PedidoItem> findByPedido(Pedido pedido);
}
