package co.dilaser.nexo.rma;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface RmaHistorialRepository extends JpaRepository<RmaHistorial, UUID> {
    List<RmaHistorial> findByRmaIdOrderByCreadoEnDesc(UUID rmaId);
}
