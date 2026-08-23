package com.example.plataforma_eventos_backend.domain.ingresso;

import com.example.plataforma_eventos_backend.domain.evento.Setor;
import com.example.plataforma_eventos_backend.domain.pedido.Pedido;
import com.example.plataforma_eventos_backend.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ingresso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Ingresso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(optional = false)
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    @Column(nullable = false, unique = true, length = 120)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusIngresso status;

    @Column(name = "validado_em")
    private OffsetDateTime validadoEm;

    @ManyToOne
    @JoinColumn(name = "validado_por")
    private User validadoPor;

    @Column(name = "token_publico", nullable = false, unique = true)
    private UUID tokenPublico;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;
}
