package co.dilaser.nexo.auth;

import co.dilaser.nexo.auth.dto.*;
import co.dilaser.nexo.usuario.Usuario;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) { return auth.login(req); }

    @PostMapping("/forgot-password")
    public Map<String, String> forgot(@Valid @RequestBody ForgotRequest req) {
        auth.forgot(req);
        return Map.of("message", "Si el correo existe, enviaremos instrucciones");
    }

    @PostMapping("/reset-password")
    public Map<String, String> reset(@Valid @RequestBody ResetRequest req) {
        auth.reset(req);
        return Map.of("message", "Contraseña actualizada");
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal Usuario u) {
        if (u == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "nombre", u.nombreCompleto(),
                "email", u.getEmail(),
                "rol", u.getRol(),
                "sede", u.getSede() == null ? "" : u.getSede(),
                "debeCambiarPass", u.isDebeCambiarPass()
        ));
    }
}
