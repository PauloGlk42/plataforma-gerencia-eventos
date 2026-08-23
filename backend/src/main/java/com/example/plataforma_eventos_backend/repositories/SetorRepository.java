package com.example.plataforma_eventos_backend.repositories;

import com.example.plataforma_eventos_backend.domain.evento.Evento;
import com.example.plataforma_eventos_backend.domain.evento.OcupacaoPorEvento;
import com.example.plataforma_eventos_backend.domain.evento.PrecoMinimoPorEvento;
import com.example.plataforma_eventos_backend.domain.evento.Setor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SetorRepository extends JpaRepository<Setor, Long> {
    List<Setor> findByEvento(Evento evento);

    /**
     * Checagem de estoque e escrita na mesma operação: 0 linhas afetadas significa que
     * não havia lugar suficiente, sem SELECT prévio e sem janela de corrida.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Setor s SET s.ocupados = s.ocupados + :quantidade " +
            "WHERE s.id = :id AND s.ocupados + :quantidade <= s.capacidade")
    int incrementarOcupados(@Param("id") Long id, @Param("quantidade") int quantidade);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Setor s SET s.ocupados = s.ocupados - :quantidade " +
            "WHERE s.id = :id AND s.ocupados - :quantidade >= 0")
    int decrementarOcupados(@Param("id") Long id, @Param("quantidade") int quantidade);

    @Query("""
            SELECT s.evento.id AS eventoId, MIN(s.preco) AS precoMinimo
            FROM Setor s WHERE s.evento IN :eventos GROUP BY s.evento.id
            """)
    List<PrecoMinimoPorEvento> buscarPrecoMinimoPorEvento(@Param("eventos") List<Evento> eventos);

    @Query("""
            SELECT s.evento.id AS eventoId, SUM(s.capacidade) AS capacidadeTotal, SUM(s.ocupados) AS ocupadosTotal
            FROM Setor s WHERE s.evento IN :eventos GROUP BY s.evento.id
            """)
    List<OcupacaoPorEvento> buscarOcupacaoPorEvento(@Param("eventos") List<Evento> eventos);
}
