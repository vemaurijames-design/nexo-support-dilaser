package co.dilaser.nexo.cliente;

import co.dilaser.nexo.common.ApiException;
import co.dilaser.nexo.equipo.Equipo;
import co.dilaser.nexo.equipo.EquipoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteRepository repo;
    private final EquipoRepository equipos;

    public ClienteController(ClienteRepository repo, EquipoRepository equipos) {
        this.repo = repo;
        this.equipos = equipos;
    }

    @GetMapping
    public List<Cliente> list(@RequestParam(required = false) String q) {
        if (q == null || q.isBlank()) return repo.findAll();
        return repo.search(q);
    }

    @GetMapping("/{id}")
    public Cliente get(@PathVariable UUID id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cliente no existe"));
    }

    @GetMapping("/{id}/equipos")
    public List<Map<String, Object>> equipos(@PathVariable UUID id) {
        get(id);
        return equipos.findByCliente_Id(id).stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("serial", e.getSerial());
            m.put("nombreEquipo", e.getNombreEquipo());
            m.put("estado", e.getEstado());
            m.put("modelo", e.getModelo() == null ? "" : e.getModelo().getNombre());
            return m;
        }).toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','LIDER_AREA')")
    public Cliente create(@RequestBody Cliente c) {
        c.setId(null);
        return repo.save(c);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','LIDER_AREA')")
    public Cliente update(@PathVariable UUID id, @RequestBody Cliente in) {
        Cliente c = get(id);
        c.setRazonSocial(in.getRazonSocial());
        c.setNombreComercial(in.getNombreComercial());
        c.setNit(in.getNit());
        c.setTipo(in.getTipo());
        c.setDireccion(in.getDireccion());
        c.setCiudad(in.getCiudad());
        c.setDepartamento(in.getDepartamento());
        c.setTelefono(in.getTelefono());
        c.setWhatsapp(in.getWhatsapp());
        c.setEmailPrincipal(in.getEmailPrincipal());
        c.setContactoNombre(in.getContactoNombre());
        c.setContactoCargo(in.getContactoCargo());
        c.setObservaciones(in.getObservaciones());
        c.setActivo(in.isActivo());
        return repo.save(c);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public void delete(@PathVariable UUID id) {
        repo.deleteById(id);
    }
}
