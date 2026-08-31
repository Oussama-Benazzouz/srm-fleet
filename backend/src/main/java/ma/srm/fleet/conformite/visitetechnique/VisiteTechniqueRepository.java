package ma.srm.fleet.conformite.visitetechnique;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisiteTechniqueRepository
    extends JpaRepository<VisiteTechnique, Long>, JpaSpecificationExecutor<VisiteTechnique> {

    /**
     * Échéances de visite technique à surveiller : pour chaque véhicule, la plus récente dont
     * la date tombe au plus tard à {@code limite} (aujourd'hui + seuil). Exclut les échéances
     * déjà renouvelées.
     */
    @Query("select v from VisiteTechnique v where v.dateEcheance <= :limite "
        + "and v.dateEcheance = (select max(x.dateEcheance) from VisiteTechnique x where x.vehicule = v.vehicule)")
    List<VisiteTechnique> findEcheancesAControler(@Param("limite") LocalDate limite);

    /**
     * Existe-t-il, pour ce véhicule, une visite technique dont la période [date, échéance]
     * chevauche [{@code debut}, {@code fin}] ? Bornes inclusives. {@code excludeId} (nullable)
     * exclut l'enregistrement en cours de modification.
     */
    @Query("select (count(v) > 0) from VisiteTechnique v where v.vehicule.id = :vehiculeId "
        + "and (:excludeId is null or v.id <> :excludeId) "
        + "and v.dateVisite <= :fin and v.dateEcheance >= :debut")
    boolean existsChevauchement(@Param("vehiculeId") Long vehiculeId,
                                @Param("debut") LocalDate debut,
                                @Param("fin") LocalDate fin,
                                @Param("excludeId") Long excludeId);
}
