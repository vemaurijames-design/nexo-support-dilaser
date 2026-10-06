package co.dilaser.nexo.remision;

import co.dilaser.nexo.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/remisiones")
public class RemisionController {
    private final RemisionRepository repo;

    public RemisionController(RemisionRepository repo) {
        this.repo = repo;
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
        UUID userId = authUserId(auth);
        Remision r = new Remision();
        r.setNumero(nextNumero());
        apply(r, body);
        r.setEstado(body.getEstado() != null ? body.getEstado() : "BORRADOR");
        r.setCreadoEn(OffsetDateTime.now());
        r.setActualizadoEn(OffsetDateTime.now());
        r.setCreadoPor(userId);
        r.setActualizadoPor(userId);
        attachItems(r, body.getItems());
        return repo.save(r);
    }

    @PutMapping("/{id}")
    @Transactional
    public Remision update(@PathVariable UUID id, @RequestBody Remision body, Authentication auth) {
        Remision r = get(id);
        apply(r, body);
        r.setActualizadoEn(OffsetDateTime.now());
        r.setActualizadoPor(authUserId(auth));
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
            n.setOrden(i++);
            r.getItems().add(n);
        }
    }

    private String nextNumero() {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long count = repo.count() + 1;
        return "REM-" + year + "-" + String.format("%05d", count);
    }

    private UUID authUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return null;
        try {
            return UUID.fromString(auth.getName());
        } catch (Exception e) {
            return null;
        }
    }
}
