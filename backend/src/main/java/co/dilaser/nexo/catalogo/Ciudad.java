package co.dilaser.nexo.catalogo;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="ciudades")
@Getter @Setter @NoArgsConstructor
public class Ciudad {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @Column(nullable=false) private String nombre;
    @Column(nullable=false) private String departamento;
}
