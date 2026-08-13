package ma.srm.fleet.carte.demande;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DemandeCarteCarburantRepository
    extends JpaRepository<DemandeCarteCarburant, Long>, JpaSpecificationExecutor<DemandeCarteCarburant> {
}
