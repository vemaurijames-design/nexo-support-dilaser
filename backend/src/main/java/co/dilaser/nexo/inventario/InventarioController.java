package co.dilaser.nexo.inventario;

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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {
    private final RepuestoRepository repuestos;
    private final StockBodegaRepository stocks;
    private final InventarioMovimientoRepository movimientos;
    private final InventarioService service;
    private final InventarioExcelService excel;
    private final PdfService pdf;
    private final EmailService email;

    public InventarioController(RepuestoRepository repuestos, StockBodegaRepository stocks,
                                InventarioMovimientoRepository movimientos, InventarioService service,
                                InventarioExcelService excel, PdfService pdf, EmailService email) {
        this.repuestos = repuestos; this.stocks = stocks; this.movimientos = movimientos;
        this.service = service; this.excel = excel; this.pdf = pdf; this.email = email;
    }

    @GetMapping("/repuestos")
    public List<Repuesto> list(@RequestParam(required=false) String q) {
        if (q == null || q.isBlank()) return repuestos.findAll();
        return repuestos.search(q);
    }

    @PostMapping("/repuestos")
    @PreAuthorize("hasAnyRole('SUPERADMIN','LIDER_AREA')")
    public Repuesto create(@RequestBody Repuesto r) {
        if (repuestos.findByReferenciaIgnoreCase(r.getReferencia()).isPresent())
            throw new ApiException(HttpStatus.CONFLICT, "Referencia ya existe");
        r.setId(null);
        return repuestos.save(r);
    }

    @PutMapping("/repuestos/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','LIDER_AREA')")
    public Repuesto update(@PathVariable UUID id, @RequestBody Repuesto in) {
        Repuesto r = repuestos.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe"));
        r.setDescripcion(in.getDescripcion());
        r.setUnidadMedida(in.getUnidadMedida());
        r.setPrecioSugerido(in.getPrecioSugerido());
        r.setStockMinimo(in.getStockMinimo());
        r.setObservaciones(in.getObservaciones());
        r.setActivo(in.isActivo());
        return repuestos.save(r);
    }

    @GetMapping("/repuestos/{id}")
    public Repuesto one(@PathVariable UUID id) {
        return repuestos.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe"));
    }

    @GetMapping("/repuestos/{id}/stock")
    public List<StockBodega> stock(@PathVariable UUID id) { return stocks.findByRepuestoId(id); }

    @GetMapping("/repuestos/{id}/kardex")
    public List<InventarioMovimiento> kardex(@PathVariable UUID id) {
        return movimientos.findByRepuestoIdOrderByCreadoEnDesc(id);
    }

    @GetMapping("/stock")
    public List<StockBodega> todoStock() { return stocks.findAll(); }

    @PostMapping("/movimientos")
    @PreAuthorize("hasAnyRole('SUPERADMIN','LIDER_AREA','INGENIERO')")
    public InventarioMovimiento mover(@RequestBody InventarioMovimiento m, @AuthenticationPrincipal Usuario u) {
        return service.mover(m, u);
    }

    @PostMapping("/importar")
    @PreAuthorize("hasAnyRole('SUPERADMIN','LIDER_AREA')")
    public Map<String, Object> importar(@RequestParam("file") MultipartFile file) {
        return excel.importar(file);
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar() {
        byte[] bytes = excel.exportar();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventario-nexo.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdfStock() {
        byte[] bytes = pdf.inventario(stocks.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=inventario.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/alertas")
    public List<Map<String, Object>> alertas() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Repuesto r : repuestos.findAll()) {
            double total = stocks.findByRepuestoId(r.getId()).stream()
                    .map(StockBodega::getExistencia).filter(java.util.Objects::nonNull)
                    .mapToDouble(java.math.BigDecimal::doubleValue).sum();
            double min = r.getStockMinimo() == null ? 0 : r.getStockMinimo().doubleValue();
            if (total <= min) {
                out.add(Map.of(
                        "id", r.getId(),
                        "referencia", r.getReferencia(),
                        "descripcion", r.getDescripcion() == null ? "" : r.getDescripcion(),
                        "existencia", total,
                        "minimo", min
                ));
            }
        }
        return out;
    }

    @PostMapping("/alertas/enviar")
    @PreAuthorize("hasAnyRole('SUPERADMIN','LIDER_AREA')")
    public Map<String, Object> enviarAlertas(@RequestBody Map<String, String> body, @AuthenticationPrincipal Usuario u) {
        String to = body.getOrDefault("to", u == null ? "" : u.getEmail());
        List<Map<String, Object>> bajas = alertas();
        StringBuilder html = new StringBuilder("<h2>Alertas de stock mínimo — Dilaser</h2><table border='1' cellpadding='6'><tr><th>Referencia</th><th>Descripción</th><th>Existencia</th><th>Mínimo</th></tr>");
        for (Map<String, Object> a : bajas) {
            html.append("<tr><td>").append(a.get("referencia")).append("</td><td>")
                    .append(a.get("descripcion")).append("</td><td>").append(a.get("existencia"))
                    .append("</td><td>").append(a.get("minimo")).append("</td></tr>");
        }
        html.append("</table><p>Solicitar compra o traslado.</p>");
        byte[] xls = excel.exportar();
        email.sendHtml(List.of(to), u == null ? List.of() : List.of(u.getEmail()),
                "Alerta stock mínimo Nexo Support",
                "<p>Adjunto el Excel con los repuestos en mínimo o sin stock. Solicitar compra.</p>",
                xls, "alertas-stock.xlsx");
        return Map.of("enviados", bajas.size(), "to", to);
    }

    @PostMapping("/repuestos/{id}/foto")
    @PreAuthorize("hasAnyRole('SUPERADMIN','LIDER_AREA','INGENIERO')")
    public Repuesto foto(@PathVariable UUID id, @RequestParam("file") MultipartFile file) throws Exception {
        Repuesto r = repuestos.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe"));
        Path dir = Path.of("uploads", "repuestos");
        Files.createDirectories(dir);
        String name = id + ".jpg";
        Files.write(dir.resolve(name), file.getBytes());
        r.setFotoNombre(name);
        return repuestos.save(r);
    }

    @GetMapping("/repuestos/{id}/foto")
    public ResponseEntity<Resource> verFoto(@PathVariable UUID id) {
        Repuesto r = repuestos.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe"));
        if (r.getFotoNombre() == null) return ResponseEntity.notFound().build();
        Path p = Path.of("uploads", "repuestos", r.getFotoNombre());
        if (!Files.exists(p)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(new FileSystemResource(p));
    }

    @GetMapping("/repuestos/{id}/pdf")
    public ResponseEntity<byte[]> ficha(@PathVariable UUID id) {
        Repuesto r = repuestos.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe"));
        byte[] bytes = pdf.fichaRepuesto(r, stocks.findByRepuestoId(id));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=repuesto.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
