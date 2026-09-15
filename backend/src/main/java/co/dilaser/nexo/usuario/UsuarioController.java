package co.dilaser.nexo.usuario;

import co.dilaser.nexo.common.ApiException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    public record UsuarioReq(@NotBlank String nombres, @NotBlank String apellidos, @Email String email,
                             String password, RolUsuario rol, String telefono, String whatsapp,
                             String sede, String cargo, Boolean activo) {}

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    public UsuarioController(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo; this.encoder = encoder;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','LIDER_AREA')")
    public List<Usuario> list() { return repo.findAll(); }

    @PostMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public Usuario create(@RequestBody UsuarioReq r) {
        if (repo.existsByEmailIgnoreCase(r.email())) throw new ApiException(HttpStatus.CONFLICT, "Email ya existe");
        return repo.save(Usuario.builder()
                .nombres(r.nombres()).apellidos(r.apellidos()).email(r.email())
                .passwordHash(encoder.encode(r.password() == null ? "Nexo.Temp.2026" : r.password()))
                .rol(r.rol()).telefono(r.telefono()).whatsapp(r.whatsapp())
                .sede(r.sede()).cargo(r.cargo()).activo(r.activo() == null || r.activo())
                .debeCambiarPass(true).build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public Usuario update(@PathVariable UUID id, @RequestBody UsuarioReq r) {
        Usuario u = repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe"));
        u.setNombres(r.nombres()); u.setApellidos(r.apellidos()); u.setRol(r.rol());
        u.setTelefono(r.telefono()); u.setWhatsapp(r.whatsapp()); u.setSede(r.sede()); u.setCargo(r.cargo());
        if (r.activo() != null) u.setActivo(r.activo());
        return repo.save(u);
    }
}
