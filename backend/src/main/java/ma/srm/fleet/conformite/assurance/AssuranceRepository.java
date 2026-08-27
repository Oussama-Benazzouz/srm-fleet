package ma.srm.fleet.conformite.assurance;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssuranceRepository
    extends JpaRepository<Assurance, Long>, JpaSpecificationExecutor<Assurance> {

    /**
     * Échéances d'assurance à surveiller : pour chaque véhicule, la plus récente (une seule
     * échéance active par type, cf. règles de gestion) dont la date tombe au plus tard à
     * {@code limite} (aujourd'hui + seuil). Exclut ainsi les échéances déjà renouvelées.
     */
    @Query("select a from Assurance a where a.dateEcheance <= :limite "
        + "and a.dateEcheance = (select max(x.dateEcheance) from Assurance x where x.vehicule = a.vehicule)")
    List<Assurance> findEcheancesAControler(@Param("limite") LocalDate limite);
}
