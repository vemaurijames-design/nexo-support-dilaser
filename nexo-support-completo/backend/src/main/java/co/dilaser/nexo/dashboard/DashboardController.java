package co.dilaser.nexo.dashboard;

import co.dilaser.nexo.equipo.EquipoRepository;
import co.dilaser.nexo.inventario.StockBodega;
import co.dilaser.nexo.inventario.StockBodegaRepository;
import co.dilaser.nexo.mantenimiento.AlertaMantenimientoRepository;
import co.dilaser.nexo.rma.RmaCasoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final EquipoRepository equipos;
    private final RmaCasoRepository rmas;
    private final StockBodegaRepository stocks;
    private final AlertaMantenimientoRepository alertas;

    public DashboardController(EquipoRepository equipos, RmaCasoRepository rmas,
                               StockBodegaRepository stocks, AlertaMantenimientoRepository alertas) {
        this.equipos = equipos; this.rmas = rmas; this.stocks = stocks; this.alertas = alertas;
    }

    @GetMapping("/kpis")
    public Map<String, Object> kpis() {
        Map<String, Object> m = new HashMap<>();
        m.put("equipos", equipos.count());
        m.put("rmas", rmas.count());
        LocalDate h = LocalDate.now();
        m.put("alertasMes", alertas.findByAnioAndMes(h.getYear(), h.getMonthValue()).size());
        BigDecimal valor = stocks.findAll().stream()
                .map(s -> s.getExistencia().multiply(s.getRepuesto().getCostoPromedio()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        m.put("valorInventario", valor);
        long criticos = stocks.findAll().stream()
                .filter(s -> s.getExistencia().compareTo(s.getRepuesto().getStockMinimo()) <= 0)
                .count();
        m.put("stockCritico", criticos);
        return m;
    }
}
