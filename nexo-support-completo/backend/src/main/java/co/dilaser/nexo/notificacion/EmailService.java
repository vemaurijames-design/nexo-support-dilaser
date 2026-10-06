package co.dilaser.nexo.notificacion;

import co.dilaser.nexo.config.NexoProperties;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templates;
    private final NexoProperties props;

    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templates, NexoProperties props) {
        this.mailSender = mailSender;
        this.templates = templates;
        this.props = props;
    }

    public void sendHtml(List<String> to, List<String> cc, String subject, String htmlBody, byte[] pdf, String pdfName) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(props.getMail().getFrom());
            helper.setTo(to.toArray(new String[0]));
            if (cc != null && !cc.isEmpty()) helper.setCc(cc.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            if (pdf != null && pdf.length > 0) {
                helper.addAttachment(pdfName != null ? pdfName : "documento.pdf", new ByteArrayResource(pdf));
            }
            mailSender.send(msg);
            log.info("Correo enviado a {} asunto={}", to, subject);
        } catch (Exception e) {
            log.error("Error enviando correo: {}", e.getMessage());
            throw new IllegalStateException("No se pudo enviar el correo: " + e.getMessage(), e);
        }
    }

    public void sendTemplate(List<String> to, List<String> cc, String subject, String template, Map<String, Object> vars, byte[] pdf, String pdfName) {
        Context ctx = new Context();
        if (vars != null) vars.forEach(ctx::setVariable);
        String html = templates.process(template, ctx);
        sendHtml(to, cc, subject, html, pdf, pdfName);
    }
}
