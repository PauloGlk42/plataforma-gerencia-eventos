package com.example.plataforma_eventos_backend.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

    @Autowired
    SecurityFilter securityFilter;

    // separadas por vírgula; vazia em dev porque o proxy do Vite evita CORS
    @Value("${app.cors.allowed-origins:}")
    private String origensPermitidas;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                                            .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                                            .requestMatchers(HttpMethod.POST, "/auth/register").permitAll() //tirar depois de decidir como vai funcionar a parte de roles
                                            .requestMatchers("/error")
                                            .permitAll()
                                            .requestMatchers(HttpMethod.GET, "/api/eventos/meus").hasRole("ORGANIZADOR")
                                            .requestMatchers(HttpMethod.GET, "/api/catalogo").hasRole("ORGANIZADOR")
                                            .requestMatchers(HttpMethod.POST, "/api/eventos/*/publicar").hasRole("ORGANIZADOR")
                                            .requestMatchers(HttpMethod.POST, "/api/eventos").hasRole("ORGANIZADOR")
                                            .requestMatchers(HttpMethod.GET, "/api/eventos/**").permitAll()
                                            .requestMatchers(HttpMethod.POST, "/api/pedidos").hasRole("CLIENTE")
                                            .requestMatchers(HttpMethod.GET, "/api/pedidos/*").hasRole("CLIENTE")
                                            .requestMatchers(HttpMethod.DELETE, "/api/pedidos/*").hasRole("CLIENTE")
                                            .requestMatchers(HttpMethod.GET, "/api/meus-pedidos").hasRole("CLIENTE")
                                            .requestMatchers(HttpMethod.POST, "/api/pedidos/*/pagamento").hasRole("CLIENTE")
                                            .requestMatchers(HttpMethod.GET, "/api/meus-ingressos").hasRole("CLIENTE")
                                            .requestMatchers(HttpMethod.POST, "/api/ingressos/*/compartilhar").hasRole("CLIENTE")
                                            .requestMatchers(HttpMethod.GET, "/api/publico/ingressos/*").permitAll()
                                            .anyRequest()
                                            .authenticated())
                                    .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                                       .build();
    }

    @Bean
    public AuthenticationManager authenticationMenager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origens = Arrays.stream(origensPermitidas.split(","))
                .map(String::trim)
                .filter(origem -> !origem.isBlank())
                .toList();

        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(origens);
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuracao);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder();}
}
