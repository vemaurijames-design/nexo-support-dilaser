package co.dilaser.nexo.inventario;

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
import org.springframework.web.multipart.MultipartFile;

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

    public InventarioController(RepuestoRepository repuestos, StockBodegaRepository stocks,
                                InventarioMovimientoRepository movimientos, InventarioService service,
                                InventarioExcelService excel, PdfService pdf) {
        this.repuestos = repuestos; this.stocks = stocks; this.movimientos = movimientos;
        this.service = service; this.excel = excel; this.pdf = pdf;
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
}
