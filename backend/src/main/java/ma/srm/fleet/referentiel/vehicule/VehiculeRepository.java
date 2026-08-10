package ma.srm.fleet.referentiel.vehicule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VehiculeRepository
    extends JpaRepository<Vehicule, Long>, JpaSpecificationExecutor<Vehicule> {

    boolean existsByImmatriculation(String immatriculation);
}
