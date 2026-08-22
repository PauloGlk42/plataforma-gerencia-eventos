package com.example.plataforma_eventos_backend.domain.evento.dtos;

import com.example.plataforma_eventos_backend.domain.evento.Setor;

import java.math.BigDecimal;

public record SetorDTO(
        Long id,
        String slug,
        String nome,
        BigDecimal preco,
        Integer capacidade,
        Integer ocupados
) {
    public static SetorDTO de(Setor setor) {
        return new SetorDTO(setor.getId(), setor.getSlug(), setor.getNome(), setor.getPreco(),
                setor.getCapacidade(), setor.getOcupados());
    }
}
