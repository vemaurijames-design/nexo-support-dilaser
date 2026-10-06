package co.dilaser.nexo.equipo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipoRepository extends JpaRepository<Equipo, UUID> {
    Optional<Equipo> findBySerialIgnoreCase(String serial);
    boolean existsBySerialIgnoreCase(String serial);
    List<Equipo> findByCliente_Id(UUID clienteId);

    @Query(value = """
        SELECT e.* FROM equipos e
        LEFT JOIN modelos_equipo m ON m.id = e.modelo_id
        LEFT JOIN clientes c ON c.id = e.cliente_id
        WHERE (CAST(:q AS text) IS NULL OR CAST(:q AS text) = ''
            OR e.serial ILIKE '%' || CAST(:q AS text) || '%'
            OR COALESCE(e.nombre_equipo, '') ILIKE '%' || CAST(:q AS text) || '%'
            OR COALESCE(m.nombre, '') ILIKE '%' || CAST(:q AS text) || '%'
            OR COALESCE(c.razon_social, '') ILIKE '%' || CAST(:q AS text) || '%')
          AND (CAST(:estado AS text) IS NULL OR CAST(:estado AS text) = '' OR e.estado = CAST(:estado AS text))
        ORDER BY e.serial
        """, nativeQuery = true)
    List<Equipo> search(@Param("q") String q, @Param("estado") String estado);
}
