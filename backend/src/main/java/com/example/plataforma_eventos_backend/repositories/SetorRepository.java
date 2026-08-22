package com.example.plataforma_eventos_backend.repositories;

import com.example.plataforma_eventos_backend.domain.evento.Evento;
import com.example.plataforma_eventos_backend.domain.evento.PrecoMinimoPorEvento;
import com.example.plataforma_eventos_backend.domain.evento.Setor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SetorRepository extends JpaRepository<Setor, Long> {
    List<Setor> findByEvento(Evento evento);

    @Query("""
            SELECT s.evento.id AS eventoId, MIN(s.preco) AS precoMinimo
            FROM Setor s WHERE s.evento IN :eventos GROUP BY s.evento.id
            """)
    List<PrecoMinimoPorEvento> buscarPrecoMinimoPorEvento(@Param("eventos") List<Evento> eventos);
}
