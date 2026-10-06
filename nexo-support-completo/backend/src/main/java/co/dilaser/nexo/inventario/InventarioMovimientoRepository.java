package co.dilaser.nexo.inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, UUID> {
    List<InventarioMovimiento> findByRepuestoIdOrderByCreadoEnDesc(UUID repuestoId);
}
