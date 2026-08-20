package ma.srm.fleet.alerte;

import java.util.List;
import ma.srm.fleet.alerte.configuration.TypeSourceAlerte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    /** Déduplication : une seule alerte par échéance source (type + identifiant de référence). */
    boolean existsByConfiguration_TypeSourceAndIdReference(TypeSourceAlerte typeSource, Long idReference);

    /** Alertes du tableau de bord selon leur statut de lecture. */
    List<Alerte> findByStatutLecture(StatutLecture statutLecture);
}
