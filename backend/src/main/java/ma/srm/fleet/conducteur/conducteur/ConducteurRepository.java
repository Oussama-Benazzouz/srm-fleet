package ma.srm.fleet.conducteur.conducteur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ConducteurRepository
    extends JpaRepository<Conducteur, Long>, JpaSpecificationExecutor<Conducteur> {

    boolean existsByMatricule(String matricule);
}
