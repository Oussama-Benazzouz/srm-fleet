package ma.srm.fleet.carte.cartecarburant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CarteCarburantRepository
    extends JpaRepository<CarteCarburant, Long>, JpaSpecificationExecutor<CarteCarburant> {

    boolean existsByNumeroCarte(String numeroCarte);

    /** RG4/RF-14 : une seule carte ACTIVE par affectation (doublé par l'index partiel en base). */
    boolean existsByAffectationIdAndStatut(Long affectationId, StatutCarte statut);

    /** Tableau de bord : nombre de cartes actives (scope province ; prov null = national). */
    @Query("select count(c) from CarteCarburant c "
        + "where c.statut = ma.srm.fleet.carte.cartecarburant.StatutCarte.ACTIVE "
        + "and (:prov is null or c.affectation.vehicule.province.id = :prov)")
    long compterActives(@Param("prov") Long prov);
}
