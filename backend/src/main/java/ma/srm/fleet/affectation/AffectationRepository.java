package ma.srm.fleet.affectation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AffectationRepository
    extends JpaRepository<Affectation, Long>, JpaSpecificationExecutor<Affectation> {

    /** RG3 (contrôle applicatif) : existe-t-il déjà une affectation ACTIVE sur ce véhicule ? */
    boolean existsByVehiculeIdAndStatut(Long vehiculeId, StatutAffectation statut);

    /** Historique par véhicule (RF-10), plus récent d'abord. */
    List<Affectation> findByVehiculeIdOrderByDateDebutDesc(Long vehiculeId);

    /** Historique par conducteur (RF-10). */
    List<Affectation> findByConducteurIdOrderByDateDebutDesc(Long conducteurId);
}
