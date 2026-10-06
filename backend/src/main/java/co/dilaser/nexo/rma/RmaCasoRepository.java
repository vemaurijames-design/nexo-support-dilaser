package co.dilaser.nexo.rma;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RmaCasoRepository extends JpaRepository<RmaCaso, UUID> {
    long countByNumeroInternoStartingWith(String prefix);

    @Query(value = """
        SELECT * FROM rma_casos r
        WHERE (CAST(:q AS text) IS NULL OR CAST(:q AS text) = ''
            OR COALESCE(r.serial_reportado, '') ILIKE '%' || CAST(:q AS text) || '%'
            OR COALESCE(r.part_number, '') ILIKE '%' || CAST(:q AS text) || '%'
            OR COALESCE(r.descripcion_item, '') ILIKE '%' || CAST(:q AS text) || '%'
            OR COALESCE(r.numero_interno, '') ILIKE '%' || CAST(:q AS text) || '%')
        ORDER BY r.creado_en DESC NULLS LAST
        """, nativeQuery = true)
    List<RmaCaso> search(@Param("q") String q);
}
