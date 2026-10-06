package co.dilaser.nexo.auth;

import co.dilaser.nexo.auth.dto.*;
import co.dilaser.nexo.common.ApiException;
import co.dilaser.nexo.config.JwtService;
import co.dilaser.nexo.config.NexoProperties;
import co.dilaser.nexo.usuario.*;
import co.dilaser.nexo.notificacion.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UsuarioRepository usuarios;
    private final PasswordResetTokenRepository resets;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final NexoProperties props;
    private final EmailService email;

    public AuthService(UsuarioRepository usuarios, PasswordResetTokenRepository resets,
                       PasswordEncoder encoder, JwtService jwt, NexoProperties props, EmailService email) {
        this.usuarios = usuarios; this.resets = resets; this.encoder = encoder; this.jwt = jwt; this.props = props; this.email = email;
    }

    public LoginResponse login(LoginRequest req) {
        Usuario u = usuarios.findByEmailIgnoreCase(req.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
        if (!u.isActivo() || !encoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        u.setUltimoLogin(OffsetDateTime.now());
        usuarios.save(u);
        return new LoginResponse(jwt.accessToken(u), jwt.refreshToken(u), u.getId(),
                u.nombreCompleto(), u.getEmail(), u.getRol(), u.isDebeCambiarPass());
    }

    @Transactional
    public void forgot(ForgotRequest req) {
        usuarios.findByEmailIgnoreCase(req.getEmail()).ifPresent(u -> {
            String raw = UUID.randomUUID().toString();
            resets.save(PasswordResetToken.builder()
                    .usuario(u).tokenHash(sha(raw))
                    .expiraEn(OffsetDateTime.now().plusMinutes(30))
                    .usado(false).creadoEn(OffsetDateTime.now())
                    .build());
            String link = props.getFrontendUrl() + "/reset-password?token=" + raw;
            log.info("Reset password para {} -> {}", u.getEmail(), link);
            String html = """
                <div style="font-family:Arial,sans-serif;color:#222">
                  <h2 style="color:#03738C">Nexo Support — Dilaser</h2>
                  <p>Recibimos una solicitud para restablecer tu contraseña.</p>
                  <p><a href="%s" style="background:#03738C;color:#fff;padding:10px 16px;text-decoration:none;border-radius:6px;display:inline-block">Restablecer contraseña</a></p>
                  <p>El enlace vence en 30 minutos. Si no fuiste tú, ignora este correo.</p>
                </div>
                """.formatted(link);
            try {
                email.sendHtml(List.of(u.getEmail()), List.of(), "Restablecer contraseña — Nexo Support", html, null, null);
            } catch (Exception ex) {
                log.error("No se pudo enviar reset a {}: {}", u.getEmail(), ex.getMessage());
            }
        });
    }

    @Transactional
    public void reset(ResetRequest req) {
        PasswordResetToken t = resets.findByTokenHashAndUsadoFalse(sha(req.getToken()))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Token inválido"));
        if (t.getExpiraEn().isBefore(OffsetDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Token vencido");
        }
        Usuario u = t.getUsuario();
        u.setPasswordHash(encoder.encode(req.getNewPassword()));
        u.setDebeCambiarPass(false);
        t.setUsado(true);
        usuarios.save(u);
        resets.save(t);
    }

    private String sha(String raw) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
