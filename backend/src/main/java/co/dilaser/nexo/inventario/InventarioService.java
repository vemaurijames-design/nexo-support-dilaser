package co.dilaser.nexo.inventario;

import co.dilaser.nexo.catalogo.Bodega;
import co.dilaser.nexo.catalogo.BodegaRepository;
import co.dilaser.nexo.common.ApiException;
import co.dilaser.nexo.usuario.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class InventarioService {
    private final RepuestoRepository repuestos;
    private final StockBodegaRepository stocks;
    private final InventarioMovimientoRepository movimientos;
    private final BodegaRepository bodegas;

    public InventarioService(RepuestoRepository repuestos, StockBodegaRepository stocks,
                             InventarioMovimientoRepository movimientos, BodegaRepository bodegas) {
        this.repuestos = repuestos; this.stocks = stocks; this.movimientos = movimientos; this.bodegas = bodegas;
    }

    @Transactional
    public InventarioMovimiento mover(InventarioMovimiento m, Usuario u) {
        Repuesto r = repuestos.findById(m.getRepuesto().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Repuesto inválido"));
        m.setRepuesto(r);
        m.setCreadoPor(u);
        if (m.getFechaMovimiento() == null) m.setFechaMovimiento(LocalDate.now());
        boolean entrada = m.getTipo() != null && m.getTipo().startsWith("ENTRADA") || "AJUSTE_POSITIVO".equals(m.getTipo()) || "RETORNO_PRESTAMO".equals(m.getTipo());
        boolean salida = m.getTipo() != null && m.getTipo().startsWith("SALIDA") || "AJUSTE_NEGATIVO".equals(m.getTipo());
        if (entrada) {
            Bodega dest = resolveBodega(m.getBodegaDestino());
            m.setBodegaDestino(dest);
            StockBodega s = stocks.findByRepuestoAndBodega(r, dest).orElseGet(() ->
                    stocks.save(StockBodega.builder().repuesto(r).bodega(dest).existencia(BigDecimal.ZERO).build()));
            if (s.getFechaPrimeraEntrada() == null) s.setFechaPrimeraEntrada(m.getFechaMovimiento());
            s.setFechaUltimaEntrada(m.getFechaMovimiento());
            if (m.getCostoUnitario() != null && m.getCostoUnitario().signum() > 0) {
                BigDecimal val = s.getExistencia().multiply(r.getCostoPromedio())
                        .add(m.getCantidad().multiply(m.getCostoUnitario()));
                BigDecimal cant = s.getExistencia().add(m.getCantidad());
                if (cant.signum() > 0) r.setCostoPromedio(val.divide(cant, 2, java.math.RoundingMode.HALF_UP));
            }
            s.setExistencia(s.getExistencia().add(m.getCantidad()));
            stocks.save(s);
            repuestos.save(r);
        } else if (salida) {
            Bodega orig = resolveBodega(m.getBodegaOrigen());
            m.setBodegaOrigen(orig);
            StockBodega s = stocks.findByRepuestoAndBodega(r, orig)
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                            "Sin stock en esa bodega. Primero registra una ENTRADA_COMPRA del ítem en la bodega origen."));
            if (s.getExistencia().compareTo(m.getCantidad()) < 0)
                throw new ApiException(HttpStatus.BAD_REQUEST, "Stock insuficiente");
            s.setExistencia(s.getExistencia().subtract(m.getCantidad()));
            s.setFechaUltimaSalida(m.getFechaMovimiento());
            stocks.save(s);
        } else if ("TRANSFERENCIA".equals(m.getTipo())) {
            Bodega orig = resolveBodega(m.getBodegaOrigen());
            Bodega dest = resolveBodega(m.getBodegaDestino());
            m.setBodegaOrigen(orig); m.setBodegaDestino(dest);
            StockBodega so = stocks.findByRepuestoAndBodega(r, orig)
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Sin stock origen"));
            if (so.getExistencia().compareTo(m.getCantidad()) < 0)
                throw new ApiException(HttpStatus.BAD_REQUEST, "Stock insuficiente");
            so.setExistencia(so.getExistencia().subtract(m.getCantidad()));
            so.setFechaUltimaSalida(m.getFechaMovimiento());
            StockBodega sd = stocks.findByRepuestoAndBodega(r, dest).orElseGet(() ->
                    stocks.save(StockBodega.builder().repuesto(r).bodega(dest).existencia(BigDecimal.ZERO).build()));
            sd.setExistencia(sd.getExistencia().add(m.getCantidad()));
            sd.setFechaUltimaEntrada(m.getFechaMovimiento());
            stocks.save(so); stocks.save(sd);
        }
        return movimientos.save(m);
    }

    public void descontarVenta(String referencia, java.math.BigDecimal cantidad, String documento, Usuario u) {
        if (referencia == null || referencia.isBlank() || cantidad == null) return;
        Repuesto r = repuestos.findAll().stream()
                .filter(x -> referencia.equalsIgnoreCase(x.getReferencia()))
                .findFirst().orElse(null);
        if (r == null) return;
        StockBodega s = stocks.findByRepuestoId(r.getId()).stream()
                .filter(x -> x.getExistencia() != null && x.getExistencia().compareTo(cantidad) >= 0)
                .findFirst().orElse(null);
        if (s == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Sin stock para " + referencia);
        }
        InventarioMovimiento m = new InventarioMovimiento();
        m.setRepuesto(r);
        m.setTipo("SALIDA_VENTA");
        m.setCantidad(cantidad);
        m.setBodegaOrigen(s.getBodega());
        m.setDocumentoRef(documento);
        m.setObservaciones("Salida por remisión " + documento);
        mover(m, u);
    }

    private Bodega resolveBodega(Bodega b) {
        if (b == null || b.getId() == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Bodega requerida");
        return bodegas.findById(b.getId()).orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Bodega inválida"));
    }
}
