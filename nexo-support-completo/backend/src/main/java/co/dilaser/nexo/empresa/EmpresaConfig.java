package co.dilaser.nexo.empresa;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "empresa_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmpresaConfig {
    @Id
    private Integer id = 1;
    @Column(name = "razon_social", nullable = false)
    private String razonSocial;
    @Column(name = "nombre_comercial")
    private String nombreComercial;
    private String nit;
    @Column(name = "direccion_medellin")
    private String direccionMedellin;
    @Column(name = "ciudad_medellin")
    private String ciudadMedellin;
    @Column(name = "telefono_medellin")
    private String telefonoMedellin;
    @Column(name = "direccion_bogota")
    private String direccionBogota;
    @Column(name = "ciudad_bogota")
    private String ciudadBogota;
    @Column(name = "telefono_bogota")
    private String telefonoBogota;
    private String email;
    private String web;
    @Column(name = "logo_ruta")
    private String logoRuta;
    @Column(name = "color_primario")
    private String colorPrimario;
    @Column(name = "color_secundario")
    private String colorSecundario;
    @Column(name = "actualizado_en")
    private OffsetDateTime actualizadoEn;
    @Column(name = "actualizado_por")
    private UUID actualizadoPor;
}
