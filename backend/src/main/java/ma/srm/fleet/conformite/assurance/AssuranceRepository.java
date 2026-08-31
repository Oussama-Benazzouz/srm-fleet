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

    /**
     * Existe-t-il, pour ce véhicule, une assurance dont la période [début, échéance] chevauche
     * [{@code debut}, {@code fin}] ? Bornes inclusives : deux périodes se chevauchent dès qu'elles
     * partagent au moins un jour. {@code excludeId} (nullable) exclut l'enregistrement en cours de
     * modification pour qu'il ne se chevauche pas avec lui-même.
     */
    @Query("select (count(a) > 0) from Assurance a where a.vehicule.id = :vehiculeId "
        + "and (:excludeId is null or a.id <> :excludeId) "
        + "and a.dateDebut <= :fin and a.dateEcheance >= :debut")
    boolean existsChevauchement(@Param("vehiculeId") Long vehiculeId,
                                @Param("debut") LocalDate debut,
                                @Param("fin") LocalDate fin,
                                @Param("excludeId") Long excludeId);
}
