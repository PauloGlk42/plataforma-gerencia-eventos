package com.example.plataforma_eventos_backend.repositories;

import com.example.plataforma_eventos_backend.domain.pagamento.Pagamento;
import com.example.plataforma_eventos_backend.domain.pedido.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    List<Pagamento> findByPedido(Pedido pedido);
}
