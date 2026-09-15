package co.dilaser.nexo.catalogo;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="marcas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Marca {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    private String codigo;
    @Column(nullable=false, unique=true) private String nombre;
    @Column(name="pais_origen") private String paisOrigen;
    @Column(nullable=false) private boolean activo = true;
}
