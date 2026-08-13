package ma.srm.fleet.carte.cartecarburant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CarteCarburantRepository
    extends JpaRepository<CarteCarburant, Long>, JpaSpecificationExecutor<CarteCarburant> {

    boolean existsByNumeroCarte(String numeroCarte);

    /** RG4/RF-14 : une seule carte ACTIVE par affectation (doublé par l'index partiel en base). */
    boolean existsByAffectationIdAndStatut(Long affectationId, StatutCarte statut);
}
