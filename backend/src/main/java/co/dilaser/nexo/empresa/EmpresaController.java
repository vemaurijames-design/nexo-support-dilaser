package co.dilaser.nexo.empresa;

import co.dilaser.nexo.common.ApiException;
import co.dilaser.nexo.config.NexoProperties;
import co.dilaser.nexo.storage.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/empresa")
public class EmpresaController {
    private final EmpresaConfigRepository repo;
    private final StorageService storage;
    private final NexoProperties props;

    public EmpresaController(EmpresaConfigRepository repo, StorageService storage, NexoProperties props) {
        this.repo = repo;
        this.storage = storage;
        this.props = props;
    }

    @GetMapping
    public EmpresaConfig get() {
        return repo.findById(1).orElseGet(() -> {
            EmpresaConfig e = EmpresaConfig.builder()
                    .id(1)
                    .razonSocial("DILASER S.A.")
                    .nombreComercial("Dilaser")
                    .nit("811.046.078-4")
                    .direccionMedellin("Cra. 33 No. 7-77")
                    .ciudadMedellin("Medellín")
                    .telefonoMedellin("(4) 311 2280")
                    .direccionBogota("Cra 7A # 123A-14 Sur · Oficina 403")
                    .ciudadBogota("Bogotá")
                    .telefonoBogota("(601) 622 3358")
                    .email("iris.p@example.org")
                    .web("www.dilaser.com.co")
                    .colorPrimario("#03738C")
                    .colorSecundario("#1F736A")
                    .actualizadoEn(OffsetDateTime.now())
                    .build();
            return repo.save(e);
        });
    }

    /** Público para login (sin token) — solo datos de marca */
    @GetMapping("/public")
    public Map<String, Object> publicInfo() {
        EmpresaConfig e = get();
        return Map.of(
                "razonSocial", nullToEmpty(e.getRazonSocial()),
                "nombreComercial", nullToEmpty(e.getNombreComercial()),
                "logoUrl", e.getLogoRuta() != null ? "/api/empresa/logo" : "",
                "colorPrimario", nullToEmpty(e.getColorPrimario()),
                "colorSecundario", nullToEmpty(e.getColorSecundario()),
                "web", nullToEmpty(e.getWeb())
        );
    }

    @GetMapping(value = "/logo", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, "image/webp", "image/svg+xml"})
    public byte[] logo() throws Exception {
        EmpresaConfig e = get();
        if (e.getLogoRuta() == null || e.getLogoRuta().isBlank()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Sin logo");
        }
        Path p = Path.of(props.getStorage().getPath(), e.getLogoRuta());
        if (!Files.exists(p)) throw new ApiException(HttpStatus.NOT_FOUND, "Logo no encontrado");
        return Files.readAllBytes(p);
    }

    @PutMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public EmpresaConfig update(@RequestBody EmpresaConfig body) {
        EmpresaConfig e = get();
        e.setRazonSocial(body.getRazonSocial());
        e.setNombreComercial(body.getNombreComercial());
        e.setNit(body.getNit());
        e.setDireccionMedellin(body.getDireccionMedellin());
        e.setCiudadMedellin(body.getCiudadMedellin());
        e.setTelefonoMedellin(body.getTelefonoMedellin());
        e.setDireccionBogota(body.getDireccionBogota());
        e.setCiudadBogota(body.getCiudadBogota());
        e.setTelefonoBogota(body.getTelefonoBogota());
        e.setEmail(body.getEmail());
        e.setWeb(body.getWeb());
        if (body.getColorPrimario() != null) e.setColorPrimario(body.getColorPrimario());
        if (body.getColorSecundario() != null) e.setColorSecundario(body.getColorSecundario());
        e.setActualizadoEn(OffsetDateTime.now());
        return repo.save(e);
    }

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPERADMIN')")
    public Map<String, String> uploadLogo(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "Archivo vacío");
        String ct = file.getContentType() != null ? file.getContentType() : "";
        if (!ct.startsWith("image/")) throw new ApiException(HttpStatus.BAD_REQUEST, "Solo imágenes");
        String stored = storage.store(file, "logo");
        EmpresaConfig e = get();
        e.setLogoRuta(stored);
        e.setActualizadoEn(OffsetDateTime.now());
        repo.save(e);
        return Map.of("logoRuta", stored, "logoUrl", "/api/empresa/logo");
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}
