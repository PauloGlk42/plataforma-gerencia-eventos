package com.example.plataforma_eventos_backend.domain.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity(name = "users")
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    private String login;
    @Enumerated(EnumType.STRING)
    private UserRoles role;
    private String password;

    public User(String name, String login, String encryptedPassword ) {
        this.name = name;
        this.login = login;
        this.password = encryptedPassword;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(this.role == UserRoles.ORGANIZADOR) return List.of(new SimpleGrantedAuthority("ROLE_ORGANIZADOR"), new SimpleGrantedAuthority("ROLE_PORTARIA"));
        if(this.role == UserRoles.CLIENTE) return List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));

        return List.of(new SimpleGrantedAuthority("ROLE_PORTARIA"));
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
