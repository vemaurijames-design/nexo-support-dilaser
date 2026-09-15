package co.dilaser.nexo.cliente;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="clientes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cliente {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(name="razon_social", nullable=false) private String razonSocial;
    @Column(name="nombre_comercial") private String nombreComercial;
    private String nit;
    @Column(nullable=false) private String tipo = "CLINICA";
    private String direccion;
    private String ciudad;
    private String departamento;
    private String telefono;
    private String whatsapp;
    @Column(name="email_principal") private String emailPrincipal;
    @Column(name="contacto_nombre") private String contactoNombre;
    @Column(name="contacto_cargo") private String contactoCargo;
    private String observaciones;
    @Column(nullable=false) private boolean activo = true;
    @Column(name="creado_en", nullable=false) private OffsetDateTime creadoEn;
    @Column(name="actualizado_en", nullable=false) private OffsetDateTime actualizadoEn;
    @PrePersist void pre() {
        var n = OffsetDateTime.now();
        if (creadoEn==null) creadoEn=n;
        actualizadoEn=n;
    }
    @PreUpdate void upd() { actualizadoEn = OffsetDateTime.now(); }
}
