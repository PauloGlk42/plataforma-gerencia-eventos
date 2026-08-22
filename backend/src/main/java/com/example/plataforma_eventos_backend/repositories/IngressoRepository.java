package com.example.plataforma_eventos_backend.repositories;

import com.example.plataforma_eventos_backend.domain.ingresso.Ingresso;
import com.example.plataforma_eventos_backend.domain.pedido.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IngressoRepository extends JpaRepository<Ingresso, Long> {
    Optional<Ingresso> findByCodigo(String codigo);
    Optional<Ingresso> findByTokenPublico(UUID tokenPublico);
    List<Ingresso> findByPedido(Pedido pedido);
}
