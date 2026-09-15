package co.dilaser.nexo.catalogo;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="bodegas")
@Getter @Setter @NoArgsConstructor
public class Bodega {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @Column(nullable=false, unique=true) private String codigo;
    @Column(nullable=false) private String nombre;
    @Column(nullable=false) private String ciudad;
    private String direccion;
    @Column(nullable=false) private boolean activa = true;
}
