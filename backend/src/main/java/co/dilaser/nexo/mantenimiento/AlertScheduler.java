package co.dilaser.nexo.mantenimiento;

import co.dilaser.nexo.equipo.Equipo;
import co.dilaser.nexo.equipo.EquipoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Component
public class AlertScheduler {
    private static final Logger log = LoggerFactory.getLogger(AlertScheduler.class);
    private final PlanMantenimientoRepository planes;
    private final AlertaMantenimientoRepository alertas;
    private final EquipoRepository equipos;
    private final JdbcTemplate jdbc;

    public AlertScheduler(PlanMantenimientoRepository planes, AlertaMantenimientoRepository alertas,
                          EquipoRepository equipos, JdbcTemplate jdbc) {
        this.planes = planes;
        this.alertas = alertas;
        this.equipos = equipos;
        this.jdbc = jdbc;
    }

    @Scheduled(cron = "0 0 6 * * *", zone = "America/Bogota")
    public void generar() {
        jdbc.execute("ALTER TABLE alertas_mantenimiento DROP CONSTRAINT IF EXISTS alertas_mantenimiento_equipo_id_anio_key");
        jdbc.execute("DROP INDEX IF EXISTS alertas_mantenimiento_equipo_id_anio_key");
        jdbc.execute("CREATE UNIQUE INDEX IF NOT EXISTS alertas_equipo_anio_mes ON alertas_mantenimiento (equipo_id, anio, mes)");
        LocalDate hoy = LocalDate.now();
        for (Equipo e : equipos.findAll()) {
            if (e.getCliente() == null) continue;
            LocalDate ancla = e.getFechaInstalacion() != null ? e.getFechaInstalacion()
                    : e.getFechaPuestaFuncionamiento() != null ? e.getFechaPuestaFuncionamiento()
                    : hoy;
            String per = e.getPeriodicidadMantenimiento() == null ? "ANUAL" : e.getPeriodicidadMantenimiento();
            int cada = per.toUpperCase().contains("SEM") ? 6 : 12;
            PlanMantenimiento plan = planes.findByActivoTrue().stream()
                    .filter(p -> p.getEquipo() != null && e.getId().equals(p.getEquipo().getId()))
                    .findFirst().orElse(null);
            for (int anio = hoy.getYear(); anio <= hoy.getYear() + 1; anio++) {
                for (int mes = 1; mes <= 12; mes++) {
                    long meses = ChronoUnit.MONTHS.between(ancla.withDayOfMonth(1), LocalDate.of(anio, mes, 1));
                    if (meses < cada || meses % cada != 0) continue;
                    int dia = Math.min(ancla.getDayOfMonth(), 28);
                    LocalDate fecha = LocalDate.of(anio, mes, dia);
                    final int anioF = anio;
                    final int mesF = mes;
                    AlertaMantenimiento a = alertas.findAll().stream()
                            .filter(x -> x.getEquipo() != null && e.getId().equals(x.getEquipo().getId())
                                    && anioF == x.getAnio() && mesF == x.getMes())
                            .findFirst()
                            .orElseGet(() -> {
                                try {
                                    return alertas.save(AlertaMantenimiento.builder()
                                    .plan(plan != null ? plan : planes.findByActivoTrue().stream().findFirst().orElse(null))
                                    .equipo(e)
                                    .cliente(e.getCliente())
                                    .anio(anioF)
                                    .mes(mesF)
                                    .fechaObjetivo(fecha)
                                    .estado("PROGRAMADA")
                                    .build());
                                } catch (Exception ex) {
                                    return null;
                                }
                            });
                    if (a == null || a.getPlan() == null) continue;
                    a.setFechaObjetivo(fecha);
                    long dias = ChronoUnit.DAYS.between(hoy, fecha);
                    if (dias < 0 && !Set.of("ACEPTADA", "CERRADA", "ENVIADA").contains(a.getEstado())) a.setEstado("VENCIDA");
                    else if (dias <= 45 && "PROGRAMADA".equals(a.getEstado())) a.setEstado("PENDIENTE_ENVIO");
                    alertas.save(a);
                }
            }
        }
        log.info("Alertas de preventivo recalculadas");
    }
}
