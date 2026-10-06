package co.dilaser.nexo.mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface AlertaMantenimientoRepository extends JpaRepository<AlertaMantenimiento, UUID> {
    Optional<AlertaMantenimiento> findByEquipoIdAndAnio(UUID equipoId, Integer anio);
    List<AlertaMantenimiento> findByAnioAndMes(Integer anio, Integer mes);
}
