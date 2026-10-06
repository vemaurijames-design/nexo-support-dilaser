package co.dilaser.nexo.inventario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepuestoRepository extends JpaRepository<Repuesto, UUID> {
    Optional<Repuesto> findByReferenciaIgnoreCase(String referencia);

    @Query(value = """
        SELECT * FROM repuestos r
        WHERE (CAST(:q AS text) IS NULL OR CAST(:q AS text) = ''
            OR r.referencia ILIKE '%' || CAST(:q AS text) || '%'
            OR r.descripcion ILIKE '%' || CAST(:q AS text) || '%')
        ORDER BY r.referencia
        """, nativeQuery = true)
    List<Repuesto> search(@Param("q") String q);
}
