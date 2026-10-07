package co.dilaser.nexo.equipo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HojaVidaMantenimientoRepository extends JpaRepository<HojaVidaMantenimiento, UUID> {
    List<HojaVidaMantenimiento> findByEquipo_IdOrderByFechaRevisionDesc(UUID equipoId);
}