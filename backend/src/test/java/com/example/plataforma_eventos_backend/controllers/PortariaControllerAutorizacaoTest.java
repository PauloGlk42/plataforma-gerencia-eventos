package com.example.plataforma_eventos_backend.controllers;

import com.example.plataforma_eventos_backend.domain.user.User;
import com.example.plataforma_eventos_backend.infra.security.TokenService;
import com.example.plataforma_eventos_backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Os três papéis (CLIENTE, ORGANIZADOR, PORTARIA) são pares: cada um só pode acessar
 * as rotas da própria authority, nunca as dos outros papéis.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PortariaControllerAutorizacaoTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenService tokenService;

    private String tokenOrganizador;

    @BeforeEach
    void setUp() {
        User organizador = (User) userRepository.findByLogin("organizador@evento.com");
        assertNotNull(organizador, "seed V8 (organizador@evento.com) precisa estar aplicado");
        tokenOrganizador = tokenService.generateToken(organizador);
    }

    @Test
    void organizadorRecebe403AoListarEventosDaPortaria() throws Exception {
        mockMvc.perform(get("/api/portaria/eventos")
                        .header("Authorization", "Bearer " + tokenOrganizador))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizadorRecebe403AoValidarIngresso() throws Exception {
        mockMvc.perform(post("/api/validacao")
                        .header("Authorization", "Bearer " + tokenOrganizador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"qualquer\",\"eventoId\":1}"))
                .andExpect(status().isForbidden());
    }

    /**
     * 403 (papel errado, sessão válida) e 401 (sem sessão válida) precisam continuar
     * distintos: o front só sabe recuperar sozinho ("sessão expirada", volta pro login) do
     * 401 — se rota protegida sem token virasse 403 igual ao de papel errado, a sessão
     * inválida ficava indistinguível e a tela travava quebrada em vez de voltar pro login.
     */
    @Test
    void semTokenRecebe401AoListarEventosDaPortaria() throws Exception {
        mockMvc.perform(get("/api/portaria/eventos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoRecebe401AoListarEventosDaPortaria() throws Exception {
        mockMvc.perform(get("/api/portaria/eventos")
                        .header("Authorization", "Bearer token.forjado.invalido"))
                .andExpect(status().isUnauthorized());
    }
}
