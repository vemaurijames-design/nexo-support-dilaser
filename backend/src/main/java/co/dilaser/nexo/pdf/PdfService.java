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
        ctx.setVariable("e", e);
        ctx.setVariable("modelo", e.getModelo() != null ? e.getModelo().getNombre() : "");
        ctx.setVariable("marca", e.getModelo() != null && e.getModelo().getMarca() != null ? e.getModelo().getMarca().getNombre() : "");
        ctx.setVariable("tec", e.getModelo() != null && e.getModelo().getTecnologia() != null ? e.getModelo().getTecnologia().getNombre() : "");
        ctx.setVariable("cliente", e.getCliente() != null ? e.getCliente().getRazonSocial() : "DILASER S.A.");
        ctx.setVariable("accesorios", e.getAccesorios() == null ? java.util.List.<EquipoAccesorio>of() : e.getAccesorios());
        ctx.setVariable("mants", e.getMantenimientos() == null ? java.util.List.<HojaVidaMantenimiento>of() : e.getMantenimientos());
        ctx.setVariable("hoy", LocalDate.now().format(FMT));
        return render("pdf/hoja-vida", ctx);
    }

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
