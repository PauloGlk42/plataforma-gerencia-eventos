package com.example.plataforma_eventos_backend.repositories;

import com.example.plataforma_eventos_backend.domain.ingresso.Ingresso;
import com.example.plataforma_eventos_backend.domain.pedido.Pedido;
import com.example.plataforma_eventos_backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    /**
     * Consumo único do ingresso: mesmo princípio do estoque de setor, sem SELECT antes.
     * 0 linhas afetadas = outra leitura concorrente (ou uma leitura anterior) já validou.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE ingresso SET status = 'UTILIZADO', validado_em = now(), validado_por = :portariaId "
            + "WHERE id = :id AND status = 'VALIDO'", nativeQuery = true)
    int marcarUtilizado(@Param("id") Long id, @Param("portariaId") String portariaId);
}
