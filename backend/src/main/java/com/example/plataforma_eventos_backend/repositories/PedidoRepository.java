package com.example.plataforma_eventos_backend.repositories;

import com.example.plataforma_eventos_backend.domain.pedido.Pedido;
import com.example.plataforma_eventos_backend.domain.pedido.StatusPedido;
import com.example.plataforma_eventos_backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByCodigo(UUID codigo);
    List<Pedido> findByCliente(User cliente);
    List<Pedido> findByStatus(StatusPedido status);
}
