package co.dilaser.nexo.rma;

import co.dilaser.nexo.catalogo.Marca;
import co.dilaser.nexo.cliente.Cliente;
import co.dilaser.nexo.equipo.Equipo;
import co.dilaser.nexo.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="rma_casos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RmaCaso {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(name="numero_interno", nullable=false, unique=true) private String numeroInterno;
    @Column(name="numero_rma_fabricante") private String numeroRmaFabricante;
    @ManyToOne @JoinColumn(name="marca_id") private Marca marca;
    @ManyToOne @JoinColumn(name="equipo_id") private Equipo equipo;
    @ManyToOne @JoinColumn(name="cliente_id") private Cliente cliente;
    @Column(name="customer_snapshot") private String customerSnapshot;
    @Column(name="descripcion_item") private String descripcionItem;
    @Column(name="serial_reportado") private String serialReportado;
    @Column(name="part_number") private String partNumber;
    @Column(name="purchase_order") private String purchaseOrder;
    @Column(name="en_garantia") private Boolean enGarantia;
    @Column(name="fecha_instalacion") private LocalDate fechaInstalacion;
    @Column(name="problema_reportado") private String problemaReportado;
    @Column(name="numero_pulsos") private Long numeroPulsos;
    @Column(name="hp_output_power") private String hpOutputPower;
    @Column(name="detalles_adicionales") private String detallesAdicionales;
    @Column(nullable=false) private String motivo = "GARANTIA";
    @Column(nullable=false) private String estado = "BORRADOR";
    @Column(name="creado_en") private OffsetDateTime creadoEn;
    @Column(name="actualizado_en") private OffsetDateTime actualizadoEn;
    @ManyToOne(optional=false) @JoinColumn(name="creado_por") private Usuario creadoPor;
    @ManyToOne @JoinColumn(name="actualizado_por") private Usuario actualizadoPor;
    @PrePersist void pre() {
        var n = OffsetDateTime.now();
        if (creadoEn==null) creadoEn=n;
        actualizadoEn=n;
    }
    @PreUpdate void upd() { actualizadoEn = OffsetDateTime.now(); }
}
