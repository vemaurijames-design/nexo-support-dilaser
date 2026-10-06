package co.dilaser.nexo.remision;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "remisiones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Remision {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true)
    private String numero;
    @Column(nullable = false)
    private LocalDate fecha;
    @Column(name = "telefono_contacto")
    private String telefonoContacto;
    @Column(name = "numero_factura")
    private String numeroFactura;
    @Column(name = "guia_numero")
    private String guiaNumero;
    @Column(name = "cliente_id")
    private UUID clienteId;
    @Column(name = "cliente_nombre", nullable = false)
    private String clienteNombre;
    @Column(name = "cliente_direccion")
    private String clienteDireccion;
    @Column(name = "empresa_destino")
    private String empresaDestino;
    /** TC | SOBREENTREGA | DEPRISA | OTROS */
    private String transportadora;
    /** VENTA | ALQUILER | PRESTAMO | DEVOLUCION | GARANTIA | REPARACION | TRASLADO */
    @Column(nullable = false)
    private String motivo;
    private String observaciones;
    @Column(name = "elaboro_nombre")
    private String elaboroNombre;
    @Column(name = "recibio_nombre")
    private String recibioNombre;
    @Column(name = "firma_elaboro")
    private String firmaElaboro;
    @Column(name = "firma_recibio")
    private String firmaRecibio;
    @Column(name = "email_destino")
    private String emailDestino;
    @Column(nullable = false)
    @Builder.Default
    private String estado = "BORRADOR";
    @Column(name = "pdf_id")
    private UUID pdfId;
    @Column(name = "creado_en")
    private OffsetDateTime creadoEn;
    @Column(name = "actualizado_en")
    private OffsetDateTime actualizadoEn;
    @Column(name = "creado_por")
    private UUID creadoPor;
    @Column(name = "actualizado_por")
    private UUID actualizadoPor;

    @OneToMany(mappedBy = "remision", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<RemisionItem> items = new ArrayList<>();
}
