package co.dilaser.nexo.catalogo;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="tecnologias")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Tecnologia {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @Column(nullable=false, unique=true) private String nombre;
    private String descripcion;
}
