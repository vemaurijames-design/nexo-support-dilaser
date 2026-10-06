package co.dilaser.nexo.inventario;
import co.dilaser.nexo.catalogo.Bodega;
import co.dilaser.nexo.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity @Table(name="inventario_movimientos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventarioMovimiento {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false) private String tipo;
    @ManyToOne(optional=false) @JoinColumn(name="repuesto_id") private Repuesto repuesto;
    @ManyToOne @JoinColumn(name="bodega_origen_id") private Bodega bodegaOrigen;
    @ManyToOne @JoinColumn(name="bodega_destino_id") private Bodega bodegaDestino;
    @Column(nullable=false) private BigDecimal cantidad;
    @Column(name="costo_unitario") private BigDecimal costoUnitario;
    @Column(name="recibido_por_nombre") private String recibidoPorNombre;
    @Column(name="documento_ref") private String documentoRef;
    private String observaciones;
    @Column(name="fecha_movimiento", nullable=false) private LocalDate fechaMovimiento = LocalDate.now();
    @Column(name="creado_en") private OffsetDateTime creadoEn = OffsetDateTime.now();
    @ManyToOne(optional=false) @JoinColumn(name="creado_por") private Usuario creadoPor;
}
