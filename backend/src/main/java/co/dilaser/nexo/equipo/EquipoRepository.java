package co.dilaser.nexo.equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface EquipoRepository extends JpaRepository<Equipo, UUID> {
    Optional<Equipo> findBySerialIgnoreCase(String serial);
    boolean existsBySerialIgnoreCase(String serial);
    @Query("""
        select e from Equipo e
        where (:q is null or lower(e.serial) like lower(concat('%', :q, '%'))
            or lower(e.modelo.nombre) like lower(concat('%', :q, '%'))
            or lower(e.cliente.razonSocial) like lower(concat('%', :q, '%')))
          and (:estado is null or e.estado = :estado)
    """)
    List<Equipo> search(String q, String estado);
}
