package co.dilaser.nexo.equipo;

import co.dilaser.nexo.catalogo.ModeloEquipo;
import co.dilaser.nexo.catalogo.ModeloEquipoRepository;
import co.dilaser.nexo.cliente.Cliente;
import co.dilaser.nexo.cliente.ClienteRepository;
import co.dilaser.nexo.common.ApiException;
import co.dilaser.nexo.mantenimiento.PlanMantenimiento;
import co.dilaser.nexo.mantenimiento.PlanMantenimientoRepository;
import co.dilaser.nexo.pdf.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/equipos")
public class EquipoController {
    private final EquipoRepository repo;
    private final ModeloEquipoRepository modelos;
    private final ClienteRepository clientes;
    private final PlanMantenimientoRepository planes;
    private final PdfService pdf;

    public EquipoController(EquipoRepository repo, ModeloEquipoRepository modelos, ClienteRepository clientes,
                            PlanMantenimientoRepository planes, PdfService pdf) {
        this.repo = repo;
        this.modelos = modelos;
        this.clientes = clientes;
        this.planes = planes;
        this.pdf = pdf;
    }

    @GetMapping
    public List<Equipo> list(@RequestParam(required = false) String q, @RequestParam(required = false) String estado) {
        return repo.search(q, estado);
    }

    @GetMapping("/{id}")
    public Equipo get(@PathVariable UUID id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Equipo no existe"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','LIDER_AREA','INGENIERO')")
    public Equipo create(@RequestBody Equipo in) {
        if (repo.existsBySerialIgnoreCase(in.getSerial()))
            throw new ApiException(HttpStatus.CONFLICT, "Serial ya existe");
        in.setId(null);
        resolveRefs(in);
        if (in.getAccesorios() != null) in.getAccesorios().forEach(a -> a.setEquipo(in));
        Equipo saved = repo.save(in);
        LocalDate ancla = saved.getFechaInstalacion() != null ? saved.getFechaInstalacion() : LocalDate.now();
        planes.save(PlanMantenimiento.builder()
                .equipo(saved)
                .periodicidad(saved.getModelo() != null ? saved.getModelo().getPeriodicidadDefault() : "ANUAL")
                .mesAncla(ancla.getMonthValue())
                .diaAncla(Math.min(ancla.getDayOfMonth(), 28))
                .proximoProgramado(ancla.plusYears(1))
                .activo(true)
                .build());
        return saved;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','LIDER_AREA','INGENIERO')")
    public Equipo update(@PathVariable UUID id, @RequestBody Equipo in) {
        Equipo e = get(id);
        e.setSerial(in.getSerial());
        e.setPropiedad(in.getPropiedad());
        e.setEstado(in.getEstado());
        e.setNombreEquipo(in.getNombreEquipo());
        e.setInstitucionNombre(in.getInstitucionNombre());
        e.setServicioUbicacion(in.getServicioUbicacion());
        e.setCodigoInterno(in.getCodigoInterno());
        e.setVersionFicha(in.getVersionFicha());
        e.setFechaFicha(in.getFechaFicha());
        e.setPaisOrigen(in.getPaisOrigen());
        e.setVoltageAlimentacion(in.getVoltageAlimentacion());
        e.setCorrienteOperacion(in.getCorrienteOperacion());
        e.setPotenciaVa(in.getPotenciaVa());
        e.setFrecuenciaHz(in.getFrecuenciaHz());
        e.setPresion(in.getPresion());
        e.setCapacidad(in.getCapacidad());
        e.setFechaImportacion(in.getFechaImportacion());
        e.setAnioFabricacion(in.getAnioFabricacion());
        e.setFechaAdquisicion(in.getFechaAdquisicion());
        e.setFechaInstalacion(in.getFechaInstalacion());
        e.setFechaPuestaFuncionamiento(in.getFechaPuestaFuncionamiento());
        e.setPesoDeclarado(in.getPesoDeclarado());
        e.setRegistroSanitario(in.getRegistroSanitario());
        e.setNumeroActaEntrega(in.getNumeroActaEntrega());
        e.setGarantiaInicio(in.getGarantiaInicio());
        e.setGarantiaFin(in.getGarantiaFin());
        e.setCiudadUbicacion(in.getCiudadUbicacion());
        e.setDireccionUbicacion(in.getDireccionUbicacion());
        e.setRepresentante(in.getRepresentante());
        e.setRepresentanteDireccion(in.getRepresentanteDireccion());
        e.setRepresentanteTelefono(in.getRepresentanteTelefono());
        e.setRepresentanteEmail(in.getRepresentanteEmail());
        e.setTecnologiaPredominante(in.getTecnologiaPredominante());
        e.setFuenteAlimentacion(in.getFuenteAlimentacion());
        e.setClasificacionBiomedica(in.getClasificacionBiomedica());
        e.setNivelRiesgo(in.getNivelRiesgo());
        e.setUsoClinico(in.getUsoClinico());
        e.setRequiereCalibracion(in.getRequiereCalibracion());
        e.setPeriodicidadCalibracion(in.getPeriodicidadCalibracion());
        e.setPeriodicidadMantenimiento(in.getPeriodicidadMantenimiento());
        e.setManuales(in.getManuales());
        e.setPlanos(in.getPlanos());
        e.setRecomendacionesFabricante(in.getRecomendacionesFabricante());
        e.setPulsosActuales(in.getPulsosActuales());
        e.setPotenciaSalidaHp(in.getPotenciaSalidaHp());
        e.setObservaciones(in.getObservaciones());
        if (in.getModelo() != null && in.getModelo().getId() != null) {
            e.setModelo(modelos.findById(in.getModelo().getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Modelo inválido")));
        }
        if (in.getCliente() != null && in.getCliente().getId() != null) {
            e.setCliente(clientes.findById(in.getCliente().getId()).orElse(e.getCliente()));
        } else if (in.getCliente() == null) {
            e.setCliente(null);
        }
        if (in.getAccesorios() != null) {
            e.getAccesorios().clear();
            in.getAccesorios().forEach(a -> {
                a.setId(null);
                a.setEquipo(e);
                e.getAccesorios().add(a);
            });
        }
        return repo.save(e);
    }

    @PostMapping("/{id}/mantenimientos")
    @PreAuthorize("hasAnyRole('SUPERADMIN','LIDER_AREA','INGENIERO')")
    public HojaVidaMantenimiento addMant(@PathVariable UUID id, @RequestBody HojaVidaMantenimiento m) {
        Equipo e = get(id);
        m.setId(null);
        m.setEquipo(e);
        e.getMantenimientos().add(m);
        repo.save(e);
        return m;
    }

    @GetMapping("/{id}/hoja-vida.pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable UUID id) {
        Equipo e = get(id);
        byte[] bytes = pdf.hojaVida(e);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=HV-" + e.getSerial() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    private void resolveRefs(Equipo in) {
        if (in.getModelo() != null && in.getModelo().getId() != null) {
            ModeloEquipo m = modelos.findById(in.getModelo().getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Modelo inválido"));
            in.setModelo(m);
        }
        if (in.getCliente() != null && in.getCliente().getId() != null) {
            Cliente c = clientes.findById(in.getCliente().getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Cliente inválido"));
            in.setCliente(c);
        }
    }
}
