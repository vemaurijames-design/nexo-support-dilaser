package co.dilaser.nexo.inventario;
import co.dilaser.nexo.catalogo.Bodega;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface StockBodegaRepository extends JpaRepository<StockBodega, UUID> {
    Optional<StockBodega> findByRepuestoAndBodega(Repuesto r, Bodega b);
    List<StockBodega> findByRepuestoId(UUID repuestoId);
}
