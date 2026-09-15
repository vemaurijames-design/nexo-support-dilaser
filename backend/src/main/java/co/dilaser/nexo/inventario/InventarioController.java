package co.dilaser.nexo.inventario;

import co.dilaser.nexo.common.ApiException;
import co.dilaser.nexo.usuario.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {
    private final RepuestoRepository repuestos;
    private final StockBodegaRepository stocks;
    private final InventarioMovimientoRepository movimientos;
    private final InventarioService service;

    public InventarioController(RepuestoRepository repuestos, StockBodegaRepository stocks,
                                InventarioMovimientoRepository movimientos, InventarioService service) {
        this.repuestos = repuestos; this.stocks = stocks; this.movimientos = movimientos; this.service = service;
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
}
