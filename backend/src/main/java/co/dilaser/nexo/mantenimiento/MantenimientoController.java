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

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/mantenimiento")
public class MantenimientoController {
    private final PlanMantenimientoRepository planes;
    private final AlertaMantenimientoRepository alertas;
    private final AlertScheduler scheduler;
    private final EmailService email;
    private final HojaVidaMantenimientoRepository historial;
    private final CronogramaImportService importer;

    public MantenimientoController(PlanMantenimientoRepository planes, AlertaMantenimientoRepository alertas,
                                   AlertScheduler scheduler, EmailService email,
                                   HojaVidaMantenimientoRepository historial, CronogramaImportService importer) {
        this.planes = planes;
        this.alertas = alertas;
        this.scheduler = scheduler;
        this.email = email;
        this.historial = historial;
        this.importer = importer;
    }

    @PostMapping("/importar")
    public Map<String, Object> importar(@RequestParam("file") MultipartFile file) throws Exception {
        return importer.importar(file);
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
        String obs = body != null && body.get("cuerpo") != null ? body.get("cuerpo") : "";
        String consecutivo = "PREV-" + a.getAnio() + "-" + String.format("%02d", a.getMes()) + "-" + serial;
        boolean garantia = a.getEquipo() != null && a.getEquipo().getGarantiaFin() != null
                && !a.getEquipo().getGarantiaFin().isBefore(a.getFechaObjetivo());
        String html = """
            <div style="font-family:Arial,sans-serif;color:#12363A;max-width:640px">
              <div style="background:#03738C;color:#fff;padding:16px 20px">
                <strong>DILASER S.A.</strong><br/>Soporte técnico · NIT 811.046.078-4
              </div>
              <div style="padding:18px 20px">
                <p>Apreciado cliente <b>%s</b>,</p>
                <p>Le informamos que el equipo relacionado tiene un mantenimiento preventivo programado. Este aviso es el consecutivo <b>%s</b>.</p>
                <table style="width:100%%;border-collapse:collapse">
                  <tr><td style="padding:6px;background:#9AD2D9">Equipo</td><td style="padding:6px">%s</td></tr>
                  <tr><td style="padding:6px;background:#9AD2D9">Serial</td><td style="padding:6px">%s</td></tr>
                  <tr><td style="padding:6px;background:#9AD2D9">Fecha del servicio</td><td style="padding:6px">%s</td></tr>
                  <tr><td style="padding:6px;background:#9AD2D9">Tipo</td><td style="padding:6px">%s</td></tr>
                </table>
                <p>%s</p>
                <p>Si desea agendar la visita o ampliar el servicio, comuníquese con el área de soporte técnico:</p>
                <p>Medellín (604) 311 2280 · Bogotá (601) 622 3358<br/>Correo: soportetecnico2@dilaser.com.co</p>
                <p>Enviado por: %s</p>
              </div>
            </div>
            """.formatted(cliente,
                consecutivo,
                a.getEquipo() != null && a.getEquipo().getNombreEquipo() != null ? a.getEquipo().getNombreEquipo() : "Equipo biomédico",
                serial, a.getFechaObjetivo(),
                garantia ? "Mantenimiento en garantía" : "Mantenimiento preventivo anual",
                obs.isBlank() ? "Este servicio corresponde al mantenimiento programado de su equipo. Para agendar la visita comuníquese con soporte técnico." : obs,
                u != null ? u.nombreCompleto() + " <" + u.getEmail() + ">" : "Soporte Técnico");
        List<String> cc = new java.util.ArrayList<>();
        if (u != null && u.getEmail() != null) cc.add(u.getEmail());
        if (body != null && body.get("cc") != null && body.get("cc").contains("@")) cc.add(body.get("cc"));
        try {
            email.sendHtml(List.of(to), cc,
                    consecutivo + " · Preventivo " + serial + " — Dilaser", html, null, null);
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
