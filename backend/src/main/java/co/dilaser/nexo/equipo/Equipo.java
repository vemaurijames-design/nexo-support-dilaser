package co.dilaser.nexo.equipo;

import co.dilaser.nexo.catalogo.ModeloEquipo;
import co.dilaser.nexo.cliente.Cliente;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "equipos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Equipo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String serial;

    @ManyToOne(optional = false)
    @JoinColumn(name = "modelo_id")
    private ModeloEquipo modelo;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(nullable = false)
    @Builder.Default
    private String propiedad = "CLIENTE";

    @Column(nullable = false)
    @Builder.Default
    private String estado = "ACTIVO";

    @Column(name = "nombre_equipo")
    private String nombreEquipo;

    @Column(name = "institucion_nombre")
    private String institucionNombre;

    @Column(name = "servicio_ubicacion")
    private String servicioUbicacion;

    @Column(name = "codigo_interno")
    private String codigoInterno;

    @Column(name = "version_ficha")
    private String versionFicha;

    @Column(name = "fecha_ficha")
    private LocalDate fechaFicha;

    @Column(name = "pais_origen")
    private String paisOrigen;

    @Column(name = "voltage_alimentacion")
    private String voltageAlimentacion;

    @Column(name = "corriente_operacion")
    private String corrienteOperacion;

    @Column(name = "potencia_va")
    private String potenciaVa;

    @Column(name = "frecuencia_hz")
    private String frecuenciaHz;

    @Column(name = "presion")
    private String presion;

    @Column(name = "capacidad")
    private String capacidad;

    @Column(name = "fecha_importacion")
    private LocalDate fechaImportacion;

    @Column(name = "anio_fabricacion")
    private Integer anioFabricacion;

    @Column(name = "fecha_adquisicion")
    private LocalDate fechaAdquisicion;

    @Column(name = "fecha_instalacion")
    private LocalDate fechaInstalacion;

    @Column(name = "fecha_puesta_funcionamiento")
    private LocalDate fechaPuestaFuncionamiento;

    @Column(name = "peso_declarado")
    private String pesoDeclarado;

    @Column(name = "registro_sanitario")
    private String registroSanitario;

    @Column(name = "numero_acta_entrega")
    private String numeroActaEntrega;

    @Column(name = "garantia_inicio")
    private LocalDate garantiaInicio;

    @Column(name = "garantia_fin")
    private LocalDate garantiaFin;

    @Column(name = "ciudad_ubicacion")
    private String ciudadUbicacion;

    @Column(name = "direccion_ubicacion")
    private String direccionUbicacion;

    @Column(name = "representante")
    private String representante;

    @Column(name = "representante_direccion")
    private String representanteDireccion;

    @Column(name = "representante_telefono")
    private String representanteTelefono;

    @Column(name = "representante_email")
    private String representanteEmail;

    @Column(name = "tecnologia_predominante")
    private String tecnologiaPredominante;

    @Column(name = "fuente_alimentacion")
    private String fuenteAlimentacion;

    @Column(name = "clasificacion_biomedica")
    private String clasificacionBiomedica;

    @Column(name = "nivel_riesgo")
    private String nivelRiesgo;

    @Column(name = "uso_clinico")
    private String usoClinico;

    @Column(name = "requiere_calibracion")
    @Builder.Default
    private Boolean requiereCalibracion = false;

    @Column(name = "periodicidad_calibracion")
    private String periodicidadCalibracion;

    @Column(name = "periodicidad_mantenimiento")
    private String periodicidadMantenimiento;

    @Column(name = "manuales")
    private String manuales;

    @Column(name = "planos")
    private String planos;

    @Column(name = "recomendaciones_fabricante")
    private String recomendacionesFabricante;

    @Column(name = "pulsos_actuales")
    private Long pulsosActuales;

    @Column(name = "potencia_salida_hp")
    private String potenciaSalidaHp;

    @Column(name = "foto_nombre")
    private String fotoNombre;

    @Column(name = "manual_nombre")
    private String manualNombre;

    private String observaciones;

    @JsonIgnore
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EquipoAccesorio> accesorios = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HojaVidaMantenimiento> mantenimientos = new ArrayList<>();

    @Column(name = "creado_en")
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en")
    private OffsetDateTime actualizadoEn;

    @JsonIgnore
    public List<EquipoAccesorio> getAccesorios() { return accesorios; }

    @JsonIgnore
    public List<HojaVidaMantenimiento> getMantenimientos() { return mantenimientos; }

    @JsonProperty("clienteNit")
    public String getClienteNit() {
        return cliente == null ? null : cliente.getNit();
    }

    @PrePersist
    void pre() {
        var n = OffsetDateTime.now();
        if (creadoEn == null) creadoEn = n;
        actualizadoEn = n;
    }

    @PreUpdate
    void up() {
        actualizadoEn = OffsetDateTime.now();
    }
}
