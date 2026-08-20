package ma.srm.fleet.conformite.assurance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AssuranceRepository
    extends JpaRepository<Assurance, Long>, JpaSpecificationExecutor<Assurance> {
}
