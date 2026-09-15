package co.dilaser.nexo.config;

import co.dilaser.nexo.usuario.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final NexoProperties props;
    public JwtService(NexoProperties props) { this.props = props; }

    private SecretKey key() {
        byte[] bytes = props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    public String accessToken(Usuario u) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(u.getId().toString())
                .claims(Map.of("email", u.getEmail(), "rol", u.getRol().name(), "nombre", u.nombreCompleto()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(props.getJwt().getAccessMinutes(), ChronoUnit.MINUTES)))
                .signWith(key())
                .compact();
    }

    public String refreshToken(Usuario u) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(u.getId().toString())
                .claim("typ", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(props.getJwt().getRefreshDays(), ChronoUnit.DAYS)))
                .signWith(key())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    }
}
