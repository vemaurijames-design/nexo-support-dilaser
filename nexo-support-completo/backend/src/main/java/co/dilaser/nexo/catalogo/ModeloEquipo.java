package co.dilaser.nexo.catalogo;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="modelos_equipo")
@Getter @Setter @NoArgsConstructor
public class ModeloEquipo {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @ManyToOne(optional=false) @JoinColumn(name="marca_id") private Marca marca;
    @ManyToOne @JoinColumn(name="linea_id") private LineaProducto linea;
    @ManyToOne @JoinColumn(name="tecnologia_id") private Tecnologia tecnologia;
    @Column(nullable=false) private String nombre;
    @Column(name="peso_aprox_kg") private java.math.BigDecimal pesoAproxKg;
    private String voltage;
    @Column(name="registro_sanitario") private String registroSanitario;
    @Column(name="periodicidad_default", nullable=false) private String periodicidadDefault = "ANUAL";
    @Column(nullable=false) private boolean activo = true;
}
