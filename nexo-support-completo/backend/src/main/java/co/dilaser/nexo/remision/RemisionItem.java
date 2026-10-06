package co.dilaser.nexo.remision;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "remision_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RemisionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remision_id", nullable = false)
    @JsonIgnore
    private Remision remision;
    private String referencia;
    @Column(name = "serial_lote")
    private String serialLote;
    @Column(nullable = false)
    private String descripcion;
    @Column(nullable = false)
    @Builder.Default
    private BigDecimal cantidad = BigDecimal.ONE;
    @Builder.Default
    private Integer orden = 0;
}
