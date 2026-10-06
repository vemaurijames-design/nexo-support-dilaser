package co.dilaser.nexo.catalogo;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="ubicaciones_bodega")
@Getter @Setter @NoArgsConstructor
public class UbicacionBodega {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @ManyToOne(optional=false) @JoinColumn(name="bodega_id") private Bodega bodega;
    @Column(nullable=false) private String codigo;
    private String descripcion;
}
