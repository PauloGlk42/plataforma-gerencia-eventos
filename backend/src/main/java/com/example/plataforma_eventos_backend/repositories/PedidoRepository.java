package com.example.plataforma_eventos_backend.repositories;

import com.example.plataforma_eventos_backend.domain.pedido.Pedido;
import com.example.plataforma_eventos_backend.domain.pedido.StatusPedido;
import com.example.plataforma_eventos_backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByCodigo(UUID codigo);
    List<Pedido> findByCliente(User cliente);
    List<Pedido> findByStatus(StatusPedido status);

    /**
     * Transição de status idempotente: WHERE id = ? AND status = statusAtual garante que,
     * se duas execuções (job + cancelamento manual, ou dois disparos do job) corram juntas,
     * só a primeira afeta 1 linha — a segunda afeta 0 e não devolve estoque de novo.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Pedido p SET p.status = :novoStatus, p.atualizadoEm = CURRENT_TIMESTAMP " +
            "WHERE p.id = :id AND p.status = :statusAtual")
    int atualizarStatusSe(@Param("id") Long id, @Param("statusAtual") StatusPedido statusAtual,
                           @Param("novoStatus") StatusPedido novoStatus);

    @Query("SELECT p.id FROM Pedido p WHERE p.status = :status AND p.expiraEm < :agora")
    List<Long> buscarIdsPendentesVencidos(@Param("status") StatusPedido status, @Param("agora") OffsetDateTime agora);
}
