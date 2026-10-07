package co.dilaser.nexo.mantenimiento;
import co.dilaser.nexo.cliente.Cliente;
import co.dilaser.nexo.equipo.Equipo;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity @Table(name="alertas_mantenimiento")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AlertaMantenimiento {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(optional=false) @JoinColumn(name="plan_id") private PlanMantenimiento plan;
    @ManyToOne(optional=false) @JoinColumn(name="equipo_id") private Equipo equipo;
    @ManyToOne(optional=false) @JoinColumn(name="cliente_id") private Cliente cliente;
    @Column(nullable=false) private Integer anio;
    @Column(nullable=false) private Integer mes;
    @Column(nullable=false) private String estado = "PROGRAMADA";
    @Column(name="fecha_objetivo", nullable=false) private LocalDate fechaObjetivo;
    @Column(name="enviado_en") private OffsetDateTime enviadoEn;
    @Column(name="enviado_a") private String enviadoA;
    private String canal;
}
