package co.dilaser.nexo.catalogo;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="lineas_producto")
@Getter @Setter @NoArgsConstructor
public class LineaProducto {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @ManyToOne(optional=false) @JoinColumn(name="marca_id") private Marca marca;
    private String codigo;
    @Column(nullable=false) private String nombre;
    @ManyToOne @JoinColumn(name="tecnologia_id") private Tecnologia tecnologia;
}
