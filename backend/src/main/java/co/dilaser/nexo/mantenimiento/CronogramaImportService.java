package co.dilaser.nexo.mantenimiento;

import co.dilaser.nexo.cliente.Cliente;
import co.dilaser.nexo.cliente.ClienteRepository;
import co.dilaser.nexo.equipo.Equipo;
import co.dilaser.nexo.equipo.EquipoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class CronogramaImportService {
    private final ClienteRepository clientes;
    private final EquipoRepository equipos;
    private final AlertaMantenimientoRepository alertas;
    private final PlanMantenimientoRepository planes;

    public CronogramaImportService(ClienteRepository clientes, EquipoRepository equipos,
                                   AlertaMantenimientoRepository alertas, PlanMantenimientoRepository planes) {
        this.clientes = clientes;
        this.equipos = equipos;
        this.alertas = alertas;
        this.planes = planes;
    }

    public Map<String, Object> importar(MultipartFile file) throws Exception {
        int creados = 0, actualizados = 0;
        try (InputStream in = file.getInputStream(); Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(0);
            Map<String, Integer> col = new HashMap<>();
            for (int i = 0; i < header.getLastCellNum(); i++) {
                Cell c = header.getCell(i);
                if (c != null) col.put(norm(c.getStringCellValue()), i);
            }
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                String nit = val(row, col.get("documento/nit"));
                String razon = val(row, col.get("cliente"));
                if (razon.isBlank() && nit.isBlank()) continue;
                Cliente cl = clientes.findByNit(nit).orElseGet(() -> {
                    Cliente c = new Cliente();
                    c.setNit(nit.isBlank() ? "SIN-NIT-" + System.currentTimeMillis() : nit);
                    c.setRazonSocial(razon.isBlank() ? "CLIENTE IMPORTADO" : razon);
                    c.setDireccion(val(row, col.get("direccion")));
                    c.setCiudad(val(row, col.get("ciudad")));
                    c.setWhatsapp(val(row, col.get("contacto celular cliente")));
                    c.setEmailPrincipal(val(row, col.get("correo")));
                    return clientes.save(c);
                });
                String equipoNombre = val(row, col.get("equipo"));
                String serial = val(row, col.get("mantenimientos equipo"));
                if (serial.isBlank()) serial = equipoNombre;
                Equipo eq = equipos.findBySerialIgnoreCase(serial).orElse(null);
                if (eq == null) {
                    eq = new Equipo();
                    eq.setSerial(serial);
                    eq.setCliente(cl);
                    eq.setNombreEquipo(equipoNombre);
                    eq.setFechaInstalacion(date(row, col.get("fecha de instalacion")));
                    eq.setGarantiaInicio(date(row, col.get("fecha inicio garantia")));
                    eq.setGarantiaFin(date(row, col.get("fecha fin garantia")));
                    equipos.save(eq);
                    creados++;
                } else {
                    actualizados++;
                }
                LocalDate fin = eq.getGarantiaFin();
                if (fin != null) {
                    int anio = fin.getYear();
                    int mes = fin.getMonthValue();
                    final Equipo eqFinal = eq;
                    boolean existe = alertas.findAll().stream().anyMatch(a ->
                            a.getEquipo() != null && a.getEquipo().getId().equals(eqFinal.getId())
                                    && a.getAnio() == anio && a.getMes() == mes);
                    if (!existe) {
                        PlanMantenimiento plan = planes.findByActivoTrue().stream().findFirst().orElse(null);
                        if (plan != null) {
                            AlertaMantenimiento a = AlertaMantenimiento.builder()
                                    .plan(plan).equipo(eq).cliente(cl)
                                    .anio(anio).mes(mes).fechaObjetivo(fin)
                                    .estado("PROGRAMADA").build();
                            alertas.save(a);
                        }
                    }
                }
            }
        }
        return Map.of("creados", creados, "actualizados", actualizados);
    }

    private String val(Row row, Integer idx) {
        if (idx == null || row.getCell(idx) == null) return "";
        Cell c = row.getCell(idx);
        return switch (c.getCellType()) {
            case STRING -> c.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(c)
                    ? c.getLocalDateTimeCellValue().toLocalDate().toString()
                    : String.valueOf((long) c.getNumericCellValue());
            default -> "";
        };
    }

    private LocalDate date(Row row, Integer idx) {
        if (idx == null || row.getCell(idx) == null) return null;
        Cell c = row.getCell(idx);
        if (c.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(c)) {
            return c.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        String s = val(row, idx);
        if (s.length() >= 10) try { return LocalDate.parse(s.substring(0, 10)); } catch (Exception ignored) {}
        return null;
    }

    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).trim()
                .replace("á","a").replace("é","e").replace("í","i").replace("ó","o").replace("ú","u");
    }
}
