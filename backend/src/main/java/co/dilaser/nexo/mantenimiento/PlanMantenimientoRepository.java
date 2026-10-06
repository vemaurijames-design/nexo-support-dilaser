package co.dilaser.nexo.mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface PlanMantenimientoRepository extends JpaRepository<PlanMantenimiento, UUID> {
    List<PlanMantenimiento> findByActivoTrue();
}
