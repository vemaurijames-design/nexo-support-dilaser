package co.dilaser.nexo.rma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;
public interface RmaCasoRepository extends JpaRepository<RmaCaso, UUID> {
    long countByNumeroInternoStartingWith(String prefix);
    @Query("""
        select r from RmaCaso r
        where (:q is null or lower(r.serialReportado) like lower(concat('%', :q, '%'))
            or lower(r.partNumber) like lower(concat('%', :q, '%'))
            or lower(r.descripcionItem) like lower(concat('%', :q, '%'))
            or lower(r.numeroInterno) like lower(concat('%', :q, '%')))
    """)
    List<RmaCaso> search(String q);
}
