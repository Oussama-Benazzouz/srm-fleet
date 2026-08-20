package ma.srm.fleet.conformite.sinistre;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SinistreRepository
    extends JpaRepository<Sinistre, Long>, JpaSpecificationExecutor<Sinistre> {
}
