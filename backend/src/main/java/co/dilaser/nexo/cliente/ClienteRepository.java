package co.dilaser.nexo.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    @Query("select c from Cliente c where lower(c.razonSocial) like lower(concat('%', :q, '%')) " +
           "or lower(coalesce(c.nit,'')) like lower(concat('%', :q, '%')) " +
           "or lower(coalesce(c.ciudad,'')) like lower(concat('%', :q, '%'))")
    List<Cliente> search(@Param("q") String q);
}
