package com.example.plataforma_eventos_backend.domain.user;

public enum UserRoles {
    CLIENTE("cliente"),
    ORGANIZADOR("organizador"),
    PORTARIA("portaria");

    private String role;

    UserRoles(String role){
        this.role = role;
    }

    public String getRole(){
        return role;
    }
}
