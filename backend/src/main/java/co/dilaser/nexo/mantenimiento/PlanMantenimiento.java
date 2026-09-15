package co.dilaser.nexo.mantenimiento;
import co.dilaser.nexo.equipo.Equipo;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;
@Entity @Table(name="planes_mantenimiento")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanMantenimiento {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @OneToOne(optional=false) @JoinColumn(name="equipo_id") private Equipo equipo;
    @Column(nullable=false) private String periodicidad = "ANUAL";
    @Column(name="mes_ancla") private Integer mesAncla;
    @Column(name="dia_ancla") private Integer diaAncla;
    @Column(name="ultimo_realizado") private LocalDate ultimoRealizado;
    @Column(name="proximo_programado", nullable=false) private LocalDate proximoProgramado;
    @Column(nullable=false) private boolean activo = true;
    private String notas;
}
