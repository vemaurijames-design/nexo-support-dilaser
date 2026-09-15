package co.dilaser.nexo.rma;

import co.dilaser.nexo.common.ApiException;
import co.dilaser.nexo.pdf.PdfService;
import co.dilaser.nexo.usuario.Usuario;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/rma")
public class RmaController {
    private final RmaCasoRepository repo;
    private final RmaHistorialRepository historial;
    private final PdfService pdf;

    public RmaController(RmaCasoRepository repo, RmaHistorialRepository historial, PdfService pdf) {
        this.repo = repo; this.historial = historial; this.pdf = pdf;
    }

    @GetMapping
    public List<RmaCaso> list(@RequestParam(required=false) String q) { return repo.search(q); }

    @GetMapping("/{id}")
    public RmaCaso get(@PathVariable UUID id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "RMA no existe"));
    }

    @GetMapping("/{id}/historial")
    public List<RmaHistorial> hist(@PathVariable UUID id) { return historial.findByRmaIdOrderByCreadoEnDesc(id); }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','LIDER_AREA','INGENIERO')")
    public RmaCaso create(@RequestBody RmaCaso in, @AuthenticationPrincipal Usuario u) {
        String prefix = "NS-RMA-" + LocalDate.now().getYear() + "-";
        long n = repo.countByNumeroInternoStartingWith(prefix) + 1;
        in.setId(null);
        in.setNumeroInterno(prefix + String.format("%05d", n));
        in.setCreadoPor(u);
        in.setActualizadoPor(u);
        if (in.getEstado() == null) in.setEstado("BORRADOR");
        RmaCaso saved = repo.save(in);
        historial.save(RmaHistorial.builder().rma(saved).usuario(u).accion("CREAR")
                .estadoNuevo(saved.getEstado()).comentario("Caso creado").creadoEn(OffsetDateTime.now()).build());
        return saved;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','LIDER_AREA','INGENIERO')")
    public RmaCaso update(@PathVariable UUID id, @RequestBody RmaCaso in, @AuthenticationPrincipal Usuario u) {
        RmaCaso r = get(id);
        String prev = r.getEstado();
        r.setNumeroRmaFabricante(in.getNumeroRmaFabricante());
        r.setCustomerSnapshot(in.getCustomerSnapshot());
        r.setDescripcionItem(in.getDescripcionItem());
        r.setSerialReportado(in.getSerialReportado());
        r.setPartNumber(in.getPartNumber());
        r.setPurchaseOrder(in.getPurchaseOrder());
        r.setEnGarantia(in.getEnGarantia());
        r.setFechaInstalacion(in.getFechaInstalacion());
        r.setProblemaReportado(in.getProblemaReportado());
        r.setNumeroPulsos(in.getNumeroPulsos());
        r.setHpOutputPower(in.getHpOutputPower());
        r.setDetallesAdicionales(in.getDetallesAdicionales());
        r.setMotivo(in.getMotivo());
        if (in.getEstado() != null) r.setEstado(in.getEstado());
        r.setActualizadoPor(u);
        RmaCaso saved = repo.save(r);
        historial.save(RmaHistorial.builder().rma(saved).usuario(u).accion("EDITAR")
                .estadoAnterior(prev).estadoNuevo(saved.getEstado())
                .comentario("Edición de " + u.nombreCompleto()).creadoEn(OffsetDateTime.now()).build());
        return saved;
    }

    @PostMapping("/{id}/enviar")
    @PreAuthorize("hasAnyRole('SUPERADMIN','LIDER_AREA','INGENIERO')")
    public Map<String, String> enviar(@PathVariable UUID id, @AuthenticationPrincipal Usuario u) {
        RmaCaso r = get(id);
        r.setEstado("ENVIADO_FABRICANTE");
        r.setActualizadoPor(u);
        repo.save(r);
        historial.save(RmaHistorial.builder().rma(r).usuario(u).accion("ENVIAR_EMAIL")
                .estadoNuevo(r.getEstado()).comentario("Marcado como enviado al fabricante")
                .creadoEn(OffsetDateTime.now()).build());
        return Map.of("message", "RMA marcado como enviado. Configure SMTP para despacho real.");
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdfDoc(@PathVariable UUID id) {
        RmaCaso r = get(id);
        byte[] bytes = pdf.rma(r);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + r.getNumeroInterno() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
