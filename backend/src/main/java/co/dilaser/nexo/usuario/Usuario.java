package co.dilaser.nexo.usuario;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 120)
    private String nombres;
    @Column(nullable = false, length = 120)
    private String apellidos;
    @Column(nullable = false, unique = true, length = 180)
    private String email;
    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RolUsuario rol;
    private String telefono;
    private String whatsapp;
    private String sede;
    private String cargo;
    @Column(nullable = false)
    private boolean activo = true;
    @Column(name = "debe_cambiar_pass", nullable = false)
    private boolean debeCambiarPass = true;
    @Column(name = "ultimo_login")
    private OffsetDateTime ultimoLogin;
    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;
    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    @PrePersist
    void pre() {
        OffsetDateTime now = OffsetDateTime.now();
        if (creadoEn == null) creadoEn = now;
        actualizadoEn = now;
    }

    @PreUpdate
    void upd() {
        actualizadoEn = OffsetDateTime.now();
    }

    public String nombreCompleto() {
        return nombres + " " + apellidos;
    }
}
