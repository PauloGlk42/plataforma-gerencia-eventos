package com.example.plataforma_eventos_backend.domain.user.dtos;

import com.example.plataforma_eventos_backend.domain.user.UserRoles;

public record LoginResponseDTO(String token, String name, UserRoles role) {
}
