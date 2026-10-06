package co.dilaser.nexo.rma;
import co.dilaser.nexo.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity @Table(name="rma_historial")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RmaHistorial {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(optional=false) @JoinColumn(name="rma_id") private RmaCaso rma;
    @ManyToOne(optional=false) @JoinColumn(name="usuario_id") private Usuario usuario;
    @Column(nullable=false) private String accion;
    @Column(name="estado_anterior") private String estadoAnterior;
    @Column(name="estado_nuevo") private String estadoNuevo;
    private String comentario;
    @Column(name="creado_en") private OffsetDateTime creadoEn = OffsetDateTime.now();
}
