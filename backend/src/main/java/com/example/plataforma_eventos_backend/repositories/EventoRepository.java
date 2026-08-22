package com.example.plataforma_eventos_backend.repositories;

import com.example.plataforma_eventos_backend.domain.evento.Evento;
import com.example.plataforma_eventos_backend.domain.evento.StatusEvento;
import com.example.plataforma_eventos_backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByStatus(StatusEvento status);
    List<Evento> findByOrganizador(User organizador);
}
