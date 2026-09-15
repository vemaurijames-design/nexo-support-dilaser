package co.dilaser.nexo.equipo;

import co.dilaser.nexo.catalogo.ModeloEquipo;
import co.dilaser.nexo.cliente.Cliente;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @Table(name="equipos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Equipo {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false, unique=true) private String serial;
    @ManyToOne(optional=false) @JoinColumn(name="modelo_id") private ModeloEquipo modelo;
    @ManyToOne @JoinColumn(name="cliente_id") private Cliente cliente;
    @Column(nullable=false) private String propiedad = "CLIENTE";
    @Column(nullable=false) private String estado = "ACTIVO";
    @Column(name="institucion_nombre") private String institucionNombre;
    @Column(name="pais_origen") private String paisOrigen;
    @Column(name="voltage_alimentacion") private String voltageAlimentacion;
    @Column(name="fecha_importacion") private LocalDate fechaImportacion;
    @Column(name="peso_declarado") private String pesoDeclarado;
    @Column(name="registro_sanitario") private String registroSanitario;
    @Column(name="fecha_instalacion") private LocalDate fechaInstalacion;
    @Column(name="numero_acta_entrega") private String numeroActaEntrega;
    @Column(name="garantia_inicio") private LocalDate garantiaInicio;
    @Column(name="garantia_fin") private LocalDate garantiaFin;
    @Column(name="ciudad_ubicacion") private String ciudadUbicacion;
    @Column(name="direccion_ubicacion") private String direccionUbicacion;
    @Column(name="pulsos_actuales") private Long pulsosActuales;
    @Column(name="potencia_salida_hp") private String potenciaSalidaHp;
    private String observaciones;
    @OneToMany(mappedBy="equipo", cascade=CascadeType.ALL, orphanRemoval=true)
    @Builder.Default
    private List<EquipoAccesorio> accesorios = new ArrayList<>();
    @OneToMany(mappedBy="equipo", cascade=CascadeType.ALL, orphanRemoval=true)
    @Builder.Default
    private List<HojaVidaMantenimiento> mantenimientos = new ArrayList<>();
    @Column(name="creado_en") private OffsetDateTime creadoEn;
    @Column(name="actualizado_en") private OffsetDateTime actualizadoEn;
    @PrePersist void pre() {
        var n = OffsetDateTime.now();
        if (creadoEn==null) creadoEn=n;
        actualizadoEn=n;
    }
    @PreUpdate void upd() { actualizadoEn = OffsetDateTime.now(); }
}
