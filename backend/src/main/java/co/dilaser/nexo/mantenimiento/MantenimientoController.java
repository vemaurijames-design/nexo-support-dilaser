package co.dilaser.nexo.mantenimiento;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/mantenimiento")
public class MantenimientoController {
    private final PlanMantenimientoRepository planes;
    private final AlertaMantenimientoRepository alertas;
    private final AlertScheduler scheduler;

    public MantenimientoController(PlanMantenimientoRepository planes, AlertaMantenimientoRepository alertas,
                                   AlertScheduler scheduler) {
        this.planes = planes; this.alertas = alertas; this.scheduler = scheduler;
    }

    @GetMapping("/planes")
    public List<PlanMantenimiento> planes() { return planes.findAll(); }

    @GetMapping("/alertas")
    public List<AlertaMantenimiento> alertas(@RequestParam(required=false) Integer anio,
                                             @RequestParam(required=false) Integer mes) {
        if (anio != null && mes != null) return alertas.findByAnioAndMes(anio, mes);
        return alertas.findAll();
    }

    @PostMapping("/alertas/recalcular")
    public void recalcular() { scheduler.generar(); }

    @GetMapping("/mes-actual")
    public List<AlertaMantenimiento> mesActual() {
        LocalDate h = LocalDate.now();
        return alertas.findByAnioAndMes(h.getYear(), h.getMonthValue());
    }
}
