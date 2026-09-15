package co.dilaser.nexo.inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface RepuestoRepository extends JpaRepository<Repuesto, UUID> {
    Optional<Repuesto> findByReferenciaIgnoreCase(String referencia);
    @Query("select r from Repuesto r where lower(r.referencia) like lower(concat('%', :q, '%')) or lower(r.descripcion) like lower(concat('%', :q, '%'))")
    List<Repuesto> search(String q);
}
