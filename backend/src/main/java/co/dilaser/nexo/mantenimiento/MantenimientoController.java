package co.dilaser.nexo.mantenimiento;

import co.dilaser.nexo.common.ApiException;
import co.dilaser.nexo.notificacion.EmailService;
import co.dilaser.nexo.usuario.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import co.dilaser.nexo.equipo.HojaVidaMantenimientoRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/mantenimiento")
public class MantenimientoController {
    private final PlanMantenimientoRepository planes;
    private final AlertaMantenimientoRepository alertas;
    private final AlertScheduler scheduler;
    private final EmailService email;
    private final HojaVidaMantenimientoRepository historial;

    public MantenimientoController(PlanMantenimientoRepository planes, AlertaMantenimientoRepository alertas,
                                   AlertScheduler scheduler, EmailService email,
                                   HojaVidaMantenimientoRepository historial) {
        this.planes = planes;
        this.alertas = alertas;
        this.scheduler = scheduler;
        this.email = email;
        this.historial = historial;
    }

    @GetMapping("/planes")
    public List<PlanMantenimiento> planes() { return planes.findAll(); }

    @GetMapping("/alertas")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<AlertaMantenimiento> alertas(@RequestParam(required = false) Integer anio,
                                             @RequestParam(required = false) Integer mes) {
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

    @PostMapping("/alertas/{id}/notificar")
    public Map<String, String> notificar(@PathVariable UUID id,
                                         @RequestBody(required = false) Map<String, String> body,
                                         Authentication auth) {
        AlertaMantenimiento a = alertas.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Alerta no existe"));
        String to = body != null ? body.get("to") : null;
        if (to == null || !to.contains("@")) {
            to = a.getCliente() != null ? a.getCliente().getEmailPrincipal() : null;
        }
        if (to == null || !to.contains("@")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El cliente no tiene email");
        }
        Usuario u = auth != null && auth.getPrincipal() instanceof Usuario usr ? usr : null;
        String serial = a.getEquipo() != null ? a.getEquipo().getSerial() : "";
        String cliente = a.getCliente() != null ? a.getCliente().getRazonSocial() : "";
        String html = """
            <div style="font-family:Arial,sans-serif">
              <h2 style="color:#03738C">Recordatorio de mantenimiento preventivo</h2>
              <p>Cliente: <b>%s</b></p>
              <p>Equipo / serial: <b>%s</b></p>
              <p>Fecha objetivo: <b>%s</b></p>
              <p>Enviado por: %s</p>
            </div>
            """.formatted(cliente, serial, a.getFechaObjetivo(),
                u != null ? u.nombreCompleto() + " <" + u.getEmail() + ">" : "Nexo Support");
        try {
            email.sendHtml(List.of(to), List.of(),
                    "Preventivo " + serial + " — Dilaser", html, null, null);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "No se pudo enviar: " + e.getMessage());
        }
        a.setEstado("ENVIADA");
        a.setCanal("EMAIL");
        a.setEnviadoA(to);
        a.setEnviadoEn(java.time.OffsetDateTime.now());
        alertas.save(a);
        return Map.of("message", "Recordatorio enviado a " + to);
    }

    @GetMapping("/historial")
    public List<Map<String, Object>> historial() {
        return historial.findAll().stream().map(h -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", h.getId());
            m.put("fecha", h.getFechaRevision());
            m.put("actividades", h.getActividades());
            m.put("tipo", h.getTipo());
            m.put("ingeniero", h.getIngenieroNombre());
            m.put("serial", h.getEquipo() != null ? h.getEquipo().getSerial() : "");
            m.put("cliente", h.getEquipo() != null && h.getEquipo().getCliente() != null
                    ? h.getEquipo().getCliente().getRazonSocial() : "");
            return m;
        }).toList();
    }
}
