package co.dilaser.nexo.remision;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface RemisionRepository extends JpaRepository<Remision, UUID> {
    List<Remision> findByClienteNombreContainingIgnoreCaseOrderByFechaDesc(String q);
    @Query("select r from Remision r order by r.fecha desc, r.numero desc")
    List<Remision> findAllOrderByFechaDesc();
}
