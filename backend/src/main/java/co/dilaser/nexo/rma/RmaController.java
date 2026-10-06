package co.dilaser.nexo.rma;

import co.dilaser.nexo.common.ApiException;
import co.dilaser.nexo.notificacion.EmailService;
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
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rma")
public class RmaController {
    private final RmaCasoRepository repo;
    private final RmaHistorialRepository historial;
    private final PdfService pdf;
    private final EmailService emailService;

    public RmaController(RmaCasoRepository repo, RmaHistorialRepository historial, PdfService pdf, EmailService emailService) {
        this.repo = repo;
        this.historial = historial;
        this.pdf = pdf;
        this.emailService = emailService;
    }

    @GetMapping
    public List<RmaCaso> list(@RequestParam(required = false) String q) {
        return repo.search(q == null ? "" : q);
    }

    @GetMapping("/{id}")
    public RmaCaso get(@PathVariable UUID id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "RMA no existe"));
    }

    @GetMapping("/{id}/historial")
    public List<RmaHistorial> hist(@PathVariable UUID id) {
        return historial.findByRmaIdOrderByCreadoEnDesc(id);
    }

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

    /**
     * Body ejemplo:
     * { "to": ["fabricante@ejemplo.com"], "cc": ["soporte@dilaser.com.co"], "asunto": "opcional" }
     */
    @PostMapping("/{id}/enviar")
    @PreAuthorize("hasAnyRole('SUPERADMIN','LIDER_AREA','INGENIERO')")
    public Map<String, String> enviar(@PathVariable UUID id,
                                      @RequestBody(required = false) Map<String, Object> body,
                                      @AuthenticationPrincipal Usuario u) {
        RmaCaso r = get(id);
        List<String> to = parseEmails(body != null ? body.get("to") : null);
        List<String> cc = parseEmails(body != null ? body.get("cc") : null);
        if (to.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Indica al menos un correo en 'to' (fabricante u otros)");
        }

        byte[] pdfBytes = pdf.rma(r);
        String asunto = body != null && body.get("asunto") != null
                ? String.valueOf(body.get("asunto"))
                : "RMA " + r.getNumeroInterno() + " — Dilaser Soporte Técnico";

        String html = """
            <div style="font-family:Arial,sans-serif;color:#222">
              <h2 style="color:#03738C">Nexo Support — Dilaser</h2>
              <p>Se adjunta el reporte RMA <strong>%s</strong>.</p>
              <ul>
                <li><b>Serial:</b> %s</li>
                <li><b>Part:</b> %s</li>
                <li><b>Cliente:</b> %s</li>
                <li><b>Problema:</b> %s</li>
              </ul>
              <p>Enviado por: %s</p>
            </div>
            """.formatted(
                r.getNumeroInterno(),
                nullSafe(r.getSerialReportado()),
                nullSafe(r.getPartNumber()),
                nullSafe(r.getCustomerSnapshot()),
                nullSafe(r.getProblemaReportado()),
                u != null ? u.nombreCompleto() : "Sistema"
        );

        try {
            emailService.sendHtml(to, cc, asunto, html, pdfBytes, r.getNumeroInterno() + ".pdf");
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "No se pudo enviar el correo. Revisa SMTP en application.properties: " + e.getMessage());
        }

        r.setEstado("ENVIADO_FABRICANTE");
        r.setActualizadoPor(u);
        repo.save(r);
        historial.save(RmaHistorial.builder().rma(r).usuario(u).accion("ENVIAR_EMAIL")
                .estadoNuevo(r.getEstado())
                .comentario("Enviado a: " + String.join(", ", to) + (cc.isEmpty() ? "" : " CC: " + String.join(", ", cc)))
                .creadoEn(OffsetDateTime.now()).build());

        return Map.of("message", "Correo enviado con PDF a " + String.join(", ", to));
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

    @SuppressWarnings("unchecked")
    private List<String> parseEmails(Object raw) {
        if (raw == null) return List.of();
        if (raw instanceof List<?> list) {
            return list.stream().filter(Objects::nonNull).map(Object::toString).map(String::trim)
                    .filter(s -> !s.isEmpty() && s.contains("@")).collect(Collectors.toList());
        }
        String s = raw.toString().trim();
        if (s.isEmpty()) return List.of();
        return Arrays.stream(s.split("[,;\\s]+")).map(String::trim)
                .filter(x -> x.contains("@")).collect(Collectors.toList());
    }

    private static String nullSafe(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }
}
