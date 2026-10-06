package co.dilaser.nexo.inventario;

import co.dilaser.nexo.catalogo.Bodega;
import co.dilaser.nexo.catalogo.BodegaRepository;
import co.dilaser.nexo.catalogo.LineaProducto;
import co.dilaser.nexo.catalogo.LineaProductoRepository;
import co.dilaser.nexo.catalogo.Marca;
import co.dilaser.nexo.catalogo.MarcaRepository;
import co.dilaser.nexo.common.ApiException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class InventarioExcelService {
    private final RepuestoRepository repuestos;
    private final StockBodegaRepository stocks;
    private final BodegaRepository bodegas;
    private final MarcaRepository marcas;
    private final LineaProductoRepository lineas;

    public InventarioExcelService(RepuestoRepository repuestos, StockBodegaRepository stocks,
                                  BodegaRepository bodegas, MarcaRepository marcas,
                                  LineaProductoRepository lineas) {
        this.repuestos = repuestos;
        this.stocks = stocks;
        this.bodegas = bodegas;
        this.marcas = marcas;
        this.lineas = lineas;
    }

    @Transactional
    public Map<String, Object> importar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Adjunta un archivo .xlsx");
        }
        int creados = 0, actualizados = 0, stockOk = 0, omitidos = 0;
        List<String> avisos = new ArrayList<>();
        try (InputStream in = file.getInputStream(); Workbook wb = WorkbookFactory.create(in)) {
            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                Sheet sheet = wb.getSheetAt(s);
                if (sheet == null) continue;
                Row header = sheet.getRow(0);
                if (header == null) continue;
                Map<String, Integer> col = headerMap(header);
                if (!col.containsKey("referencia") && !col.containsKey("ref")) {
                    continue;
                }
                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    String ref = text(row, col, "referencia", "ref");
                    String desc = text(row, col, "desc. item", "desc item", "descripcion", "descripción");
                    if (ref.isBlank() || ref.equalsIgnoreCase("referencia")) {
                        omitidos++;
                        continue;
                    }
                    if (desc.isBlank()) desc = ref;
                    Optional<Repuesto> existing = repuestos.findByReferenciaIgnoreCase(ref.trim());
                    Repuesto item = existing.orElseGet(Repuesto::new);
                    boolean isNew = item.getId() == null;
                    item.setReferencia(ref.trim());
                    item.setDescripcion(desc.trim());
                    String um = text(row, col, "u.m.", "um", "unidad", "unidad_medida");
                    item.setUnidadMedida(um.isBlank() ? "UND" : um.trim());
                    BigDecimal costo = num(row, col, "costo prom. unit. (ins)", "costo", "precio unitario", "costo_promedio");
                    if (costo != null) item.setCostoPromedio(costo);
                    BigDecimal precio = num(row, col, "precio de venta sugerido unitario", "precio sugerido", "precio_sugerido");
                    if (precio != null) item.setPrecioSugerido(precio);
                    String obs = text(row, col, "observaciones", "observacion");
                    if (!obs.isBlank()) item.setObservaciones(obs);
                    String ubi = text(row, col, "ubicacion", "ubicación");
                    if (!ubi.isBlank()) item.setUbicacion(ubi);
                    item.setActivo(true);
                    String marcaNom = text(row, col, "marca");
                    Marca marca = null;
                    if (!marcaNom.isBlank()) {
                        marca = findOrCreateMarca(marcaNom);
                        item.setMarca(marca);
                    }
                    String lineaNom = text(row, col, "linea", "línea");
                    if (!lineaNom.isBlank()) item.setLinea(findOrCreateLinea(lineaNom, marca));
                    final Repuesto guardado = repuestos.save(item);
                    if (isNew) creados++; else actualizados++;

                    BigDecimal exist = num(row, col, "existencia");
                    Bodega bodega = resolveBodega(
                            text(row, col, "bodega"),
                            text(row, col, "desc. bodega", "desc bodega", "bodega_nombre"),
                            sheet.getSheetName()
                    );
                    if (exist != null && bodega != null) {
                        StockBodega st = stocks.findByRepuestoAndBodega(guardado, bodega).orElseGet(() ->
                                StockBodega.builder().repuesto(guardado).bodega(bodega).existencia(BigDecimal.ZERO).build());
                        st.setExistencia(exist);
                        LocalDate f1 = date(row, col, "fecha primera entrada");
                        LocalDate fu = date(row, col, "fecha última entrada", "fecha ultima entrada");
                        LocalDate fv = date(row, col, "fecha última venta", "fecha ultima venta");
                        if (f1 != null) st.setFechaPrimeraEntrada(f1);
                        if (fu != null) st.setFechaUltimaEntrada(fu);
                        if (fv != null) st.setFechaUltimaSalida(fv);
                        stocks.save(st);
                        stockOk++;
                    } else if (exist != null && bodega == null) {
                        avisos.add(ref + ": existencia sin bodega reconocida (hoja " + sheet.getSheetName() + ")");
                    }
                }
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No se pudo leer el Excel: " + e.getMessage());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("creados", creados);
        out.put("actualizados", actualizados);
        out.put("stockActualizado", stockOk);
        out.put("omitidos", omitidos);
        out.put("avisos", avisos);
        out.put("message", "Importados " + creados + " nuevos y " + actualizados + " actualizados. Stock en " + stockOk + " filas.");
        return out;
    }

    public byte[] exportar() {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Inventario");
            String[] headers = {
                    "Referencia", "Desc. item", "Bodega", "Desc. bodega", "MARCA", "LINEA",
                    "Fecha primera entrada", "Fecha última entrada", "Fecha última venta",
                    "U.M.", "Existencia", "Costo prom. unit. (ins)", "Precio de venta sugerido unitario",
                    "Observaciones"
            };
            Row h = sh.createRow(0);
            for (int i = 0; i < headers.length; i++) h.createCell(i).setCellValue(headers[i]);
            int row = 1;
            List<StockBodega> all = stocks.findAll();
            if (all.isEmpty()) {
                for (Repuesto r : repuestos.findAll()) {
                    Row rw = sh.createRow(row++);
                    rw.createCell(0).setCellValue(nz(r.getReferencia()));
                    rw.createCell(1).setCellValue(nz(r.getDescripcion()));
                    rw.createCell(9).setCellValue(nz(r.getUnidadMedida()));
                    rw.createCell(11).setCellValue(r.getCostoPromedio() == null ? 0 : r.getCostoPromedio().doubleValue());
                    rw.createCell(12).setCellValue(r.getPrecioSugerido() == null ? 0 : r.getPrecioSugerido().doubleValue());
                    rw.createCell(13).setCellValue(nz(r.getObservaciones()));
                }
            } else {
                for (StockBodega s : all) {
                    Repuesto r = s.getRepuesto();
                    Bodega b = s.getBodega();
                    Row rw = sh.createRow(row++);
                    rw.createCell(0).setCellValue(r != null ? nz(r.getReferencia()) : "");
                    rw.createCell(1).setCellValue(r != null ? nz(r.getDescripcion()) : "");
                    rw.createCell(2).setCellValue(b != null ? nz(b.getCodigo()) : "");
                    rw.createCell(3).setCellValue(b != null ? nz(b.getNombre()) : "");
                    rw.createCell(4).setCellValue(r != null && r.getMarca() != null ? nz(r.getMarca().getNombre()) : "");
                    rw.createCell(5).setCellValue(r != null && r.getLinea() != null ? nz(r.getLinea().getNombre()) : "");
                    if (s.getFechaPrimeraEntrada() != null) rw.createCell(6).setCellValue(s.getFechaPrimeraEntrada().toString());
                    if (s.getFechaUltimaEntrada() != null) rw.createCell(7).setCellValue(s.getFechaUltimaEntrada().toString());
                    if (s.getFechaUltimaSalida() != null) rw.createCell(8).setCellValue(s.getFechaUltimaSalida().toString());
                    rw.createCell(9).setCellValue(r != null ? nz(r.getUnidadMedida()) : "UND");
                    rw.createCell(10).setCellValue(s.getExistencia() == null ? 0 : s.getExistencia().doubleValue());
                    rw.createCell(11).setCellValue(r != null && r.getCostoPromedio() != null ? r.getCostoPromedio().doubleValue() : 0);
                    rw.createCell(12).setCellValue(r != null && r.getPrecioSugerido() != null ? r.getPrecioSugerido().doubleValue() : 0);
                    rw.createCell(13).setCellValue(r != null ? nz(r.getObservaciones()) : "");
                }
            }
            for (int i = 0; i < headers.length; i++) sh.autoSizeColumn(i);
            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo exportar: " + e.getMessage());
        }
    }

    private Bodega resolveBodega(String codigo, String nombre, String sheetName) {
        List<Bodega> all = bodegas.findAll();
        String c = codigo == null ? "" : codigo.trim();
        String n = nombre == null ? "" : nombre.trim();
        for (Bodega b : all) {
            if (!c.isBlank() && c.equalsIgnoreCase(nz(b.getCodigo()))) return b;
        }
        for (Bodega b : all) {
            if (!n.isBlank() && n.equalsIgnoreCase(nz(b.getNombre()))) return b;
        }
        String sh = sheetName == null ? "" : sheetName.toLowerCase();
        for (Bodega b : all) {
            String bn = (b.getNombre() + " " + b.getCiudad()).toLowerCase();
            if (sh.contains("medell") && bn.contains("medell")) return b;
            if (sh.contains("bogot") && bn.contains("bogot")) return b;
        }
        return all.size() == 1 ? all.get(0) : null;
    }

    private Marca findOrCreateMarca(String raw) {
        String name = raw.replaceAll("^[0-9]+\\s*-\\s*", "").trim();
        return marcas.findAll().stream()
                .filter(m -> m.getNombre() != null && m.getNombre().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Marca m = new Marca();
                    m.setNombre(name);
                    m.setActivo(true);
                    return marcas.save(m);
                });
    }

    private LineaProducto findOrCreateLinea(String raw, Marca marca) {
        String name = raw.replaceAll("^[0-9]+\\s*-\\s*", "").trim();
        return lineas.findAll().stream()
                .filter(l -> l.getNombre() != null && l.getNombre().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    if (marca == null) return null;
                    LineaProducto l = new LineaProducto();
                    l.setNombre(name);
                    l.setMarca(marca);
                    return lineas.save(l);
                });
    }

    private Map<String, Integer> headerMap(Row header) {
        Map<String, Integer> m = new HashMap<>();
        for (Cell c : header) {
            String k = norm(val(c));
            if (!k.isBlank()) m.put(k, c.getColumnIndex());
        }
        return m;
    }

    private String text(Row row, Map<String, Integer> col, String... keys) {
        for (String k : keys) {
            Integer i = col.get(norm(k));
            if (i != null) {
                String v = val(row.getCell(i));
                if (!v.isBlank()) return v;
            }
        }
        return "";
    }

    private BigDecimal num(Row row, Map<String, Integer> col, String... keys) {
        String t = text(row, col, keys);
        if (t.isBlank()) return null;
        t = t.replace("%", "").replace(" ", "").replace(",", ".");
        try { return new BigDecimal(t); } catch (Exception e) { return null; }
    }

    private LocalDate date(Row row, Map<String, Integer> col, String... keys) {
        for (String k : keys) {
            Integer i = col.get(norm(k));
            if (i == null) continue;
            Cell c = row.getCell(i);
            if (c == null) continue;
            try {
                if (c.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(c)) {
                    return c.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                }
            } catch (Exception ignored) {}
            String t = val(c);
            if (t.length() >= 10) {
                try { return LocalDate.parse(t.substring(0, 10)); } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private String val(Cell c) {
        if (c == null) return "";
        return switch (c.getCellType()) {
            case STRING -> c.getStringCellValue() == null ? "" : c.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(c)
                    ? c.getLocalDateTimeCellValue().toLocalDate().toString()
                    : BigDecimal.valueOf(c.getNumericCellValue()).stripTrailingZeros().toPlainString();
            case BOOLEAN -> String.valueOf(c.getBooleanCellValue());
            case FORMULA -> {
                try { yield BigDecimal.valueOf(c.getNumericCellValue()).stripTrailingZeros().toPlainString(); }
                catch (Exception e) { yield c.toString(); }
            }
            default -> "";
        };
    }

    private static String norm(String s) {
        if (s == null) return "";
        String t = s.toLowerCase(Locale.ROOT).trim();
        t = t.replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u");
        return t.replaceAll("\\s+", " ");
    }

    private static String nz(String s) { return s == null ? "" : s; }
}
