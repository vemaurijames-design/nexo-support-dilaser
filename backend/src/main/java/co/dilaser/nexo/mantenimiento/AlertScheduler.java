package co.dilaser.nexo.mantenimiento;

import co.dilaser.nexo.equipo.Equipo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public AlertScheduler(PlanMantenimientoRepository planes, AlertaMantenimientoRepository alertas) {
        this.planes = planes;
        this.alertas = alertas;
    }

    @Scheduled(cron = "0 0 6 * * *", zone = "America/Bogota")
    public void generar() {
        LocalDate hoy = LocalDate.now();
        for (PlanMantenimiento p : planes.findByActivoTrue()) {
            Equipo e = p.getEquipo();
            if (e.getCliente() == null) {
                continue;
            }
            int mes = p.getMesAncla() != null ? p.getMesAncla() : hoy.getMonthValue();
            int dia = p.getDiaAncla() != null ? p.getDiaAncla() : 15;

            LocalDate candidato = LocalDate.of(hoy.getYear(), mes, Math.min(dia, 28));
            // Variable final para poder usarla dentro del lambda
            final LocalDate proximo = candidato.isBefore(hoy) ? candidato.plusYears(1) : candidato;
            final int anio = proximo.getYear();

            AlertaMantenimiento a = alertas.findByEquipoIdAndAnio(e.getId(), anio).orElseGet(() ->
                    alertas.save(AlertaMantenimiento.builder()
                            .plan(p)
                            .equipo(e)
                            .cliente(e.getCliente())
                            .anio(anio)
                            .mes(mes)
                            .fechaObjetivo(proximo)
                            .estado("PROGRAMADA")
                            .build()));

            long dias = ChronoUnit.DAYS.between(hoy, proximo);
            if (dias <= 45 && "PROGRAMADA".equals(a.getEstado())) {
                a.setEstado("PENDIENTE_ENVIO");
            }
            if (dias < 0 && !Set.of("ACEPTADA", "CERRADA").contains(a.getEstado())) {
                a.setEstado("VENCIDA");
            }
            alertas.save(a);
        }
        log.info("Alertas de preventivo recalculadas");
    }
}
