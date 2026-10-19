package co.dilaser.nexo.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Optional<Cliente> findByNit(String nit);

    @Query(value = """
        SELECT * FROM clientes c
        WHERE (CAST(:q AS text) IS NULL OR CAST(:q AS text) = ''
            OR c.razon_social ILIKE '%' || CAST(:q AS text) || '%'
            OR COALESCE(c.nit, '') ILIKE '%' || CAST(:q AS text) || '%'
            OR COALESCE(c.ciudad, '') ILIKE '%' || CAST(:q AS text) || '%')
        ORDER BY c.razon_social
        """, nativeQuery = true)
    List<Cliente> search(@Param("q") String q);
}
