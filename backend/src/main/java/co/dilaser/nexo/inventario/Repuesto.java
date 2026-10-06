package co.dilaser.nexo.inventario;
import co.dilaser.nexo.catalogo.LineaProducto;
import co.dilaser.nexo.catalogo.Marca;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity @Table(name="repuestos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Repuesto {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false, unique=true) private String referencia;
    @Column(nullable=false) private String descripcion;
    @ManyToOne @JoinColumn(name="marca_id") private Marca marca;
    @ManyToOne @JoinColumn(name="linea_id") private LineaProducto linea;
    @Column(name="unidad_medida", nullable=false) private String unidadMedida = "UND";
    @Column(name="costo_promedio", nullable=false) private BigDecimal costoPromedio = BigDecimal.ZERO;
    @Column(name="precio_sugerido", nullable=false) private BigDecimal precioSugerido = BigDecimal.ZERO;
    @Column(name="stock_minimo", nullable=false) private BigDecimal stockMinimo = BigDecimal.ZERO;
    @Column(nullable=false) private boolean activo = true;
    private String observaciones;
    private String ubicacion;
    @Column(name="foto_nombre") private String fotoNombre;
    @Column(name="creado_en") private OffsetDateTime creadoEn;
    @Column(name="actualizado_en") private OffsetDateTime actualizadoEn;
    @PrePersist void pre() {
        var n = OffsetDateTime.now();
        if (creadoEn==null) creadoEn=n;
        actualizadoEn=n;
    }
    @PreUpdate void upd() { actualizadoEn = OffsetDateTime.now(); }
}
