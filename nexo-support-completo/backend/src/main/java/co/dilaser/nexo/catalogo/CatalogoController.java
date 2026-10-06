package co.dilaser.nexo.catalogo;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {
    private final MarcaRepository marcas;
    private final TecnologiaRepository tecnologias;
    private final LineaProductoRepository lineas;
    private final ModeloEquipoRepository modelos;
    private final BodegaRepository bodegas;
    private final CiudadRepository ciudades;

    public CatalogoController(MarcaRepository marcas, TecnologiaRepository tecnologias,
                              LineaProductoRepository lineas, ModeloEquipoRepository modelos,
                              BodegaRepository bodegas, CiudadRepository ciudades) {
        this.marcas = marcas; this.tecnologias = tecnologias; this.lineas = lineas;
        this.modelos = modelos; this.bodegas = bodegas; this.ciudades = ciudades;
    }

    @GetMapping
    public Map<String, List<?>> all() {
        return Map.of(
                "marcas", marcas.findAll(),
                "tecnologias", tecnologias.findAll(),
                "lineas", lineas.findAll(),
                "modelos", modelos.findAll(),
                "bodegas", bodegas.findAll(),
                "ciudades", ciudades.findAll()
        );
    }
    @GetMapping("/marcas") public List<Marca> marcas() { return marcas.findAll(); }
    @GetMapping("/tecnologias") public List<Tecnologia> tec() { return tecnologias.findAll(); }
    @GetMapping("/lineas") public List<LineaProducto> lineas() { return lineas.findAll(); }
    @GetMapping("/modelos") public List<ModeloEquipo> modelos() { return modelos.findAll(); }
    @GetMapping("/bodegas") public List<Bodega> bodegas() { return bodegas.findAll(); }
    @GetMapping("/ciudades") public List<Ciudad> ciudades() { return ciudades.findAll(); }
}
