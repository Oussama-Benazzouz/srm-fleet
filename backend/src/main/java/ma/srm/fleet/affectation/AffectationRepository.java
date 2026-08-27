package ma.srm.fleet.affectation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AffectationRepository
    extends JpaRepository<Affectation, Long>, JpaSpecificationExecutor<Affectation> {

    /** RG3 (contrôle applicatif) : existe-t-il déjà une affectation ACTIVE sur ce véhicule ? */
    boolean existsByVehiculeIdAndStatut(Long vehiculeId, StatutAffectation statut);

    /** Historique par véhicule (RF-10), plus récent d'abord. */
    List<Affectation> findByVehiculeIdOrderByDateDebutDesc(Long vehiculeId);

    /** Historique par conducteur (RF-10). */
    List<Affectation> findByConducteurIdOrderByDateDebutDesc(Long conducteurId);

    /**
     * RG11 : affectation(s) du véhicule couvrant une date donnée, la plus récente d'abord.
     * Sert à déterminer le conducteur accompagnateur d'une visite technique (titulaire de
     * l'affectation active à la date de la visite) et à rattacher un sinistre à l'affectation
     * active au moment des faits (RG7).
     */
    @Query("select a from Affectation a where a.vehicule.id = :vehiculeId "
        + "and a.dateDebut <= :date and (a.dateFin is null or a.dateFin >= :date) "
        + "order by a.dateDebut desc")
    List<Affectation> findCouvrantDate(@Param("vehiculeId") Long vehiculeId, @Param("date") LocalDate date);

    /** Tableau de bord : nombre d'affectations actives (scope province ; prov null = national). */
    @Query("select count(a) from Affectation a where a.statut = ma.srm.fleet.affectation.StatutAffectation.ACTIVE "
        + "and (:prov is null or a.vehicule.province.id = :prov)")
    long compterActives(@Param("prov") Long prov);
}
