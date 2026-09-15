package co.dilaser.nexo.config;

import co.dilaser.nexo.usuario.RolUsuario;
import co.dilaser.nexo.usuario.Usuario;
import co.dilaser.nexo.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public AdminBootstrap(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        String email = "soportetecnico2@dilaser.com.co";
        if (!usuarios.existsByEmailIgnoreCase(email)) {
            usuarios.save(Usuario.builder()
                    .nombres("Soporte").apellidos("Técnico")
                    .email(email)
                    .passwordHash(encoder.encode("admin123456"))
                    .rol(RolUsuario.SUPERADMIN)
                    .sede("Medellín").cargo("Super Administrador")
                    .activo(true).debeCambiarPass(false)
                    .build());
            log.info("Usuario SUPERADMIN creado: {} / admin123456", email);
        }
    }
}
