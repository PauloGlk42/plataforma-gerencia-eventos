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
    /**
     * Só preenchido para PORTARIA: o organizador que criou este acesso. CLIENTE e
     * ORGANIZADOR ficam com null. Guardado como id solto (sem relação JPA) porque o User
     * autenticado é carregado fora de transação (SecurityFilter, com open-in-view=false) —
     * uma associação lazy aqui quebraria com LazyInitializationException ao ser acessada
     * depois, dentro do service.
     */
    @Column(name = "organizador_id")
    private String organizadorId;

    public User(String name, String login, String encryptedPassword ) {
        this.name = name;
        this.login = login;
        this.password = encryptedPassword;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
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
