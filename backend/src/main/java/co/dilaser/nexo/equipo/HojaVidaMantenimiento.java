package co.dilaser.nexo.equipo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity @Table(name="hoja_vida_mantenimientos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HojaVidaMantenimiento {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(optional=false) @JoinColumn(name="equipo_id") @JsonIgnore private Equipo equipo;
    @Column(name="fecha_revision", nullable=false) private LocalDate fechaRevision;
    @Column(nullable=false) private String actividades;
    @Column(nullable=false) private String tipo = "PREVENTIVO";
    @Column(name="informe_codigo") private String informeCodigo;
    @Column(name="ingeniero_nombre") private String ingenieroNombre;
    @Column(name="creado_en") private OffsetDateTime creadoEn = OffsetDateTime.now();
}
