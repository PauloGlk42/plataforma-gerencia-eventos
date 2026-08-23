package com.example.plataforma_eventos_backend.repositories;

import com.example.plataforma_eventos_backend.domain.ingresso.Ingresso;
import com.example.plataforma_eventos_backend.domain.pedido.Pedido;
import com.example.plataforma_eventos_backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IngressoRepository extends JpaRepository<Ingresso, Long> {
    Optional<Ingresso> findByCodigo(String codigo);
    Optional<Ingresso> findByTokenPublico(UUID tokenPublico);
    List<Ingresso> findByPedido(Pedido pedido);

    @Query("SELECT i FROM Ingresso i WHERE i.pedido.cliente = :cliente ORDER BY i.pedido.evento.inicio, i.id")
    List<Ingresso> findByCliente(@Param("cliente") User cliente);
}
