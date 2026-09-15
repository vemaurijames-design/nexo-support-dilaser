package co.dilaser.nexo.auth;

import co.dilaser.nexo.auth.dto.*;
import co.dilaser.nexo.common.ApiException;
import co.dilaser.nexo.config.JwtService;
import co.dilaser.nexo.config.NexoProperties;
import co.dilaser.nexo.usuario.*;
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
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UsuarioRepository usuarios;
    private final PasswordResetTokenRepository resets;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final NexoProperties props;

    public AuthService(UsuarioRepository usuarios, PasswordResetTokenRepository resets,
                       PasswordEncoder encoder, JwtService jwt, NexoProperties props) {
        this.usuarios = usuarios; this.resets = resets; this.encoder = encoder; this.jwt = jwt; this.props = props;
    }

    public LoginResponse login(LoginRequest req) {
        Usuario u = usuarios.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
        if (!u.isActivo() || !encoder.matches(req.password(), u.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        u.setUltimoLogin(OffsetDateTime.now());
        usuarios.save(u);
        return new LoginResponse(jwt.accessToken(u), jwt.refreshToken(u), u.getId(),
                u.nombreCompleto(), u.getEmail(), u.getRol(), u.isDebeCambiarPass());
    }

    @Transactional
    public void forgot(ForgotRequest req) {
        usuarios.findByEmailIgnoreCase(req.email()).ifPresent(u -> {
            String raw = UUID.randomUUID().toString();
            resets.save(PasswordResetToken.builder()
                    .usuario(u).tokenHash(sha(raw))
                    .expiraEn(OffsetDateTime.now().plusMinutes(30))
                    .usado(false).creadoEn(OffsetDateTime.now())
                    .build());
            String link = props.getFrontendUrl() + "/reset-password?token=" + raw;
            log.info("Reset password para {} -> {}", u.getEmail(), link);
            // En producción: EmailService.sendReset(u, link)
        });
    }

    @Transactional
    public void reset(ResetRequest req) {
        PasswordResetToken t = resets.findByTokenHashAndUsadoFalse(sha(req.token()))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Token inválido"));
        if (t.getExpiraEn().isBefore(OffsetDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Token vencido");
        }
        Usuario u = t.getUsuario();
        u.setPasswordHash(encoder.encode(req.newPassword()));
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
