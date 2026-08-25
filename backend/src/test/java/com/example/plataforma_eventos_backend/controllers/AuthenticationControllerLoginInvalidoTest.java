package com.example.plataforma_eventos_backend.controllers;

import com.example.plataforma_eventos_backend.infra.security.TokenService;
import com.example.plataforma_eventos_backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Login inválido (senha errada ou e-mail inexistente) precisa virar 401 com mensagem clara,
 * nunca o 500 genérico do handler de Exception — e a mensagem não pode diferenciar os dois
 * casos, senão entrega ao atacante quais e-mails estão cadastrados.
 */
@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerLoginInvalidoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private TokenService tokenService;

    @Test
    void senhaErradaDevolve401ComMensagemClara() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"cliente1@evento.com\",\"password\":\"senha-errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha incorretos"));
    }

    @Test
    void loginInexistenteDevolveMesmoStatusEMensagemQueSenhaErrada() throws Exception {
        // AuthorizationService devolve usuário nulo pra login desconhecido; o Spring Security
        // converte isso em InternalAuthenticationServiceException — mesma resposta da senha
        // errada, senão a diferença de mensagem já denunciaria quais e-mails existem.
        when(authenticationManager.authenticate(any()))
                .thenThrow(new InternalAuthenticationServiceException("UserDetailsService returned null"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"nao-cadastrado@evento.com\",\"password\":\"qualquer\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha incorretos"));
    }
}
