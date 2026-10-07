package co.dilaser.nexo.pdf;

import co.dilaser.nexo.equipo.Equipo;
import co.dilaser.nexo.equipo.EquipoAccesorio;
import co.dilaser.nexo.equipo.HojaVidaMantenimiento;
import co.dilaser.nexo.rma.RmaCaso;
import co.dilaser.nexo.remision.Remision;
import co.dilaser.nexo.inventario.StockBodega;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {
    private final SpringTemplateEngine engine;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PdfService(SpringTemplateEngine engine) { this.engine = engine; }

    public byte[] hojaVida(Equipo e) {
        Context ctx = new Context();
        ctx.setVariable("hoy", LocalDate.now().format(FMT));
        ctx.setVariable("serial", nz(e.getSerial()));
        ctx.setVariable("nombre", nz(e.getNombreEquipo()));
        ctx.setVariable("ciudad", nz(e.getCiudadUbicacion()));
        ctx.setVariable("direccion", nz(e.getDireccionUbicacion()));
        ctx.setVariable("pais", nz(e.getPaisOrigen()));
        ctx.setVariable("voltage", nz(e.getVoltageAlimentacion()));
        ctx.setVariable("peso", nz(e.getPesoDeclarado()));
        ctx.setVariable("invima", nz(e.getRegistroSanitario()));
        ctx.setVariable("importacion", e.getFechaImportacion() == null ? "" : e.getFechaImportacion().format(FMT));
        ctx.setVariable("modelo", e.getModelo() != null ? nz(e.getModelo().getNombre()) : "");
        ctx.setVariable("marca", e.getModelo() != null && e.getModelo().getMarca() != null ? nz(e.getModelo().getMarca().getNombre()) : "");
        ctx.setVariable("tec", e.getModelo() != null && e.getModelo().getTecnologia() != null ? nz(e.getModelo().getTecnologia().getNombre()) : nz(e.getTecnologiaPredominante()));
        ctx.setVariable("cliente", e.getCliente() != null ? nz(e.getCliente().getRazonSocial()) : "DILASER S.A.");
        ctx.setVariable("nit", e.getCliente() != null ? nz(e.getCliente().getNit()) : "811.046.078-4");
        java.util.List<java.util.Map<String, String>> acc = new java.util.ArrayList<>();
        if (e.getAccesorios() != null) {
            for (EquipoAccesorio a : e.getAccesorios()) {
                acc.add(java.util.Map.of(
                        "descripcion", nz(a.getDescripcion()),
                        "cantidad", a.getCantidad() == null ? "1" : a.getCantidad().toPlainString(),
                        "serial", nz(a.getSerialAccesorio())));
            }
        }
        java.util.List<java.util.Map<String, String>> mants = new java.util.ArrayList<>();
        if (e.getMantenimientos() != null) {
            for (HojaVidaMantenimiento h : e.getMantenimientos()) {
                mants.add(java.util.Map.of(
                        "fecha", h.getFechaRevision() == null ? "" : h.getFechaRevision().format(FMT),
                        "actividades", nz(h.getActividades()),
                        "ingeniero", nz(h.getIngenieroNombre())));
            }
        }
        ctx.setVariable("accesorios", acc);
        ctx.setVariable("mants", mants);
        return render("pdf/hoja-vida", ctx);
    }

    private static String nz(String s) { return s == null ? "" : s; }

    public byte[] rma(RmaCaso r) {
        Context ctx = new Context();
        ctx.setVariable("r", r);
        ctx.setVariable("hoy", LocalDate.now().format(FMT));
        return render("pdf/rma", ctx);
    }

    public byte[] remision(Remision r) {
        Context ctx = new Context();
        ctx.setVariable("r", r);
        ctx.setVariable("items", r.getItems() == null ? java.util.List.of() : r.getItems());
        ctx.setVariable("hoy", LocalDate.now().format(FMT));
        String firma = r.getFirmaElaboro();
        ctx.setVariable("firmaImg", firma != null && firma.startsWith("data:image") ? firma : null);
        return render("pdf/remision", ctx);
    }

    public byte[] inventario(java.util.List<StockBodega> stock) {
        Context ctx = new Context();
        ctx.setVariable("stock", stock == null ? java.util.List.of() : stock);
        ctx.setVariable("hoy", LocalDate.now().format(FMT));
        return render("pdf/inventario", ctx);
    }

    public byte[] fichaRepuesto(co.dilaser.nexo.inventario.Repuesto r, java.util.List<StockBodega> stock) {
        Context ctx = new Context();
        ctx.setVariable("r", r);
        ctx.setVariable("stock", stock);
        ctx.setVariable("hoy", LocalDate.now().format(FMT));
        return render("pdf/repuesto", ctx);
    }

    private byte[] render(String template, Context ctx) {
        String html = engine.process(template, ctx);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder b = new PdfRendererBuilder();
            b.useFastMode();
            b.withHtmlContent(html, null);
            b.toStream(baos);
            b.run();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar PDF: " + ex.getMessage(), ex);
        }
    }
}
