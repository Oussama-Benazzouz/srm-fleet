package ma.srm.fleet.conformite.document;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    /** Pièces jointes d'un sinistre, plus récentes d'abord. */
    List<Document> findBySinistreIdOrderByDateUploadDesc(Long sinistreId);
}
