package co.dilaser.nexo.remision;

import co.dilaser.nexo.common.ApiException;
import co.dilaser.nexo.notificacion.EmailService;
import co.dilaser.nexo.inventario.InventarioService;
import co.dilaser.nexo.pdf.PdfService;
import co.dilaser.nexo.usuario.Usuario;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/remisiones")
public class RemisionController {
    private final RemisionRepository repo;
    private final PdfService pdf;
    private final EmailService emailService;
    private final InventarioService inventario;

    public RemisionController(RemisionRepository repo, PdfService pdf, EmailService emailService,
                              InventarioService inventario) {
        this.repo = repo;
        this.pdf = pdf;
        this.emailService = emailService;
        this.inventario = inventario;
    }

    @GetMapping
    public List<Remision> list(@RequestParam(required = false) String q) {
        if (q != null && !q.isBlank()) {
            return repo.findByClienteNombreContainingIgnoreCaseOrderByFechaDesc(q.trim());
        }
        return repo.findAllOrderByFechaDesc();
    }

    @GetMapping("/{id}")
    public Remision get(@PathVariable UUID id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Remisión no encontrada"));
    }

    @PostMapping
    @Transactional
    public Remision create(@RequestBody Remision body, Authentication auth) {
        Usuario u = principal(auth);
        Remision r = new Remision();
        r.setNumero(nextNumero());
        apply(r, body);
        if (r.getElaboroNombre() == null || r.getElaboroNombre().isBlank()) {
            r.setElaboroNombre(u != null ? u.nombreCompleto() : "Soporte Técnico");
        }
        r.setEstado(body.getEstado() != null ? body.getEstado() : "BORRADOR");
        r.setCreadoEn(OffsetDateTime.now());
        r.setActualizadoEn(OffsetDateTime.now());
        r.setCreadoPor(u != null ? u.getId() : null);
        r.setActualizadoPor(u != null ? u.getId() : null);
        attachItems(r, body.getItems());
        Remision saved = repo.save(r);
        descontar(saved, u);
        return saved;
    }

    @PutMapping("/{id}")
    @Transactional
    public Remision update(@PathVariable UUID id, @RequestBody Remision body, Authentication auth) {
        Remision r = get(id);
        apply(r, body);
        r.setActualizadoEn(OffsetDateTime.now());
        Usuario u = principal(auth);
        r.setActualizadoPor(u != null ? u.getId() : null);
        r.getItems().clear();
        attachItems(r, body.getItems());
        return repo.save(r);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) throw new ApiException(HttpStatus.NOT_FOUND, "No existe");
        repo.deleteById(id);
        return Map.of("status", "ok");
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdfDoc(@PathVariable UUID id) {
        Remision r = get(id);
        byte[] bytes = pdf.remision(r);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + r.getNumero() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @PostMapping("/{id}/enviar")
    public Map<String, String> enviar(@PathVariable UUID id,
                                      @RequestBody(required = false) Map<String, Object> body,
                                      Authentication auth) {
        Remision r = get(id);
        List<String> to = parseEmails(body != null ? body.get("to") : null);
        if (to.isEmpty() && r.getEmailDestino() != null) to = parseEmails(r.getEmailDestino());
        if (to.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "Indica el correo destino");
        List<String> cc = parseEmails(body != null ? body.get("cc") : null);
        String asunto = body != null && body.get("asunto") != null
                ? String.valueOf(body.get("asunto"))
                : "Remisión " + r.getNumero() + " — Dilaser";
        String extra = body != null && body.get("cuerpo") != null ? String.valueOf(body.get("cuerpo")) : "";
        Usuario u = principal(auth);
        String html = """
            <div style="font-family:Arial,sans-serif;color:#222">
              <h2 style="color:#03738C">Remisión %s</h2>
              <p>%s</p>
              <p>Cliente: <b>%s</b><br/>Fecha: %s<br/>Motivo: %s<br/>Elaboró: %s</p>
              <p>Se adjunta el PDF de la remisión.</p>
            </div>
            """.formatted(r.getNumero(), extra, n(r.getClienteNombre()), r.getFecha(), n(r.getMotivo()),
                n(r.getElaboroNombre()));
        try {
            emailService.sendHtml(to, cc, asunto, html, pdf.remision(r), r.getNumero() + ".pdf");
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "No se pudo enviar: " + e.getMessage());
        }
        r.setEstado("ENVIADA");
        r.setEmailDestino(String.join(",", to));
        repo.save(r);
        return Map.of("message", "Remisión enviada a " + String.join(", ", to),
                "enviadoPor", u != null ? u.getEmail() : "");
    }

    private void descontar(Remision r, Usuario u) {
        if (r.getItems() == null) return;
        for (RemisionItem it : r.getItems()) {
            String motivo = it.getMotivo() == null ? r.getMotivo() : it.getMotivo();
            if (motivo != null && motivo.toUpperCase().contains("VENTA")) {
                inventario.descontarVenta(it.getReferencia(), it.getCantidad(), r.getNumero(), u);
            }
        }
    }

    private void apply(Remision r, Remision body) {
        if (body.getFecha() != null) r.setFecha(body.getFecha());
        else if (r.getFecha() == null) r.setFecha(LocalDate.now());
        r.setTelefonoContacto(body.getTelefonoContacto());
        r.setNumeroFactura(body.getNumeroFactura());
        r.setGuiaNumero(body.getGuiaNumero());
        r.setClienteId(body.getClienteId());
        r.setClienteNombre(body.getClienteNombre());
        r.setClienteDireccion(body.getClienteDireccion());
        r.setEmpresaDestino(body.getEmpresaDestino());
        r.setTransportadora(body.getTransportadora() != null ? body.getTransportadora() : "OTROS");
        r.setMotivo(body.getMotivo() != null ? body.getMotivo() : "VENTA");
        r.setObservaciones(body.getObservaciones());
        r.setElaboroNombre(body.getElaboroNombre());
        r.setRecibioNombre(body.getRecibioNombre());
        r.setFirmaElaboro(body.getFirmaElaboro());
        r.setFirmaRecibio(body.getFirmaRecibio());
        r.setEmailDestino(body.getEmailDestino());
        if (body.getEstado() != null) r.setEstado(body.getEstado());
    }

    private void attachItems(Remision r, List<RemisionItem> items) {
        if (items == null) return;
        int i = 0;
        for (RemisionItem it : items) {
            RemisionItem n = new RemisionItem();
            n.setRemision(r);
            n.setReferencia(it.getReferencia());
            n.setSerialLote(it.getSerialLote());
            n.setDescripcion(it.getDescripcion());
            n.setCantidad(it.getCantidad() != null ? it.getCantidad() : java.math.BigDecimal.ONE);
            n.setMotivo(it.getMotivo() != null ? it.getMotivo() : r.getMotivo());
            n.setOrden(i++);
            r.getItems().add(n);
        }
    }

    private String nextNumero() {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long count = repo.count() + 1;
        return "REM-" + year + "-" + String.format("%05d", count);
    }

    private Usuario principal(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Usuario u) return u;
        return null;
    }

    private List<String> parseEmails(Object raw) {
        if (raw == null) return List.of();
        if (raw instanceof List<?> list) {
            return list.stream().filter(Objects::nonNull).map(Object::toString).map(String::trim)
                    .filter(s -> s.contains("@")).collect(Collectors.toList());
        }
        return Arrays.stream(raw.toString().split("[,;\\s]+")).map(String::trim)
                .filter(s -> s.contains("@")).collect(Collectors.toList());
    }

    private static String n(String s) { return s == null || s.isBlank() ? "—" : s; }
}
