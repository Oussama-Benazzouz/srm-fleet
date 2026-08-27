package ma.srm.fleet.conformite.sinistre;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SinistreRepository
    extends JpaRepository<Sinistre, Long>, JpaSpecificationExecutor<Sinistre> {

    /** Tableau de bord : répartition des sinistres par statut (scope province ; prov null = national). */
    @Query("select s.statut, count(s) from Sinistre s "
        + "where (:prov is null or s.vehicule.province.id = :prov) group by s.statut")
    List<Object[]> compterParStatut(@Param("prov") Long prov);
}
