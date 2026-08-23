package com.example.plataforma_eventos_backend.controllers;

import com.example.plataforma_eventos_backend.domain.user.User;
import com.example.plataforma_eventos_backend.domain.user.UserRoles;
import com.example.plataforma_eventos_backend.domain.user.dtos.AuthenticationDTO;
import com.example.plataforma_eventos_backend.domain.user.dtos.LoginResponseDTO;
import com.example.plataforma_eventos_backend.domain.user.dtos.RegisterDTO;
import com.example.plataforma_eventos_backend.infra.security.TokenService;
import com.example.plataforma_eventos_backend.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthenticationDTO data){
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((User) auth.getPrincipal());
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid RegisterDTO data){
        if(this.userRepository.findByLogin(data.login()) != null) return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User(data.name(), data.login(), encryptedPassword);
        // só o cliente se autorregistra por aqui; organizador e portaria são provisionados
        // de outra forma (fora do escopo atual), então o cadastro público é sempre CLIENTE
        newUser.setRole(UserRoles.CLIENTE);

        this.userRepository.save(newUser);

        return ResponseEntity.ok().build();

    }


}
