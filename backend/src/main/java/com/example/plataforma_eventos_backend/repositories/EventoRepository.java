package com.example.plataforma_eventos_backend.repositories;

import com.example.plataforma_eventos_backend.domain.evento.Evento;
import com.example.plataforma_eventos_backend.domain.evento.StatusEvento;
import com.example.plataforma_eventos_backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long>, JpaSpecificationExecutor<Evento> {
    List<Evento> findByStatus(StatusEvento status);
    List<Evento> findByStatusOrderByInicio(StatusEvento status);
    List<Evento> findByStatusAndOrganizador_IdOrderByInicio(StatusEvento status, String organizadorId);
    List<Evento> findByOrganizador(User organizador);
}
