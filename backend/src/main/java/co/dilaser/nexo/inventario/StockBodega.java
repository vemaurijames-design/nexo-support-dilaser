package co.dilaser.nexo.inventario;
import co.dilaser.nexo.catalogo.Bodega;
import co.dilaser.nexo.catalogo.UbicacionBodega;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
@Entity @Table(name="stock_bodega")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockBodega {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(optional=false) @JoinColumn(name="repuesto_id") private Repuesto repuesto;
    @ManyToOne(optional=false) @JoinColumn(name="bodega_id") private Bodega bodega;
    @ManyToOne @JoinColumn(name="ubicacion_id") private UbicacionBodega ubicacion;
    @Column(nullable=false) private BigDecimal existencia = BigDecimal.ZERO;
    @Column(name="fecha_primera_entrada") private LocalDate fechaPrimeraEntrada;
    @Column(name="fecha_ultima_entrada") private LocalDate fechaUltimaEntrada;
    @Column(name="fecha_ultima_salida") private LocalDate fechaUltimaSalida;
}
