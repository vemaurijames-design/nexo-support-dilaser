package co.dilaser.nexo.equipo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;
@Entity @Table(name="equipo_accesorios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EquipoAccesorio {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(optional=false) @JoinColumn(name="equipo_id") @JsonIgnore private Equipo equipo;
    @Column(nullable=false) private String descripcion;
    @Column(nullable=false) private BigDecimal cantidad = BigDecimal.ONE;
    @Column(name="serial_accesorio") private String serialAccesorio;
    @Column(nullable=false) private int orden = 0;
}
