package ma.srm.fleet.conducteur.conducteur;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConducteurRepository extends JpaRepository<Conducteur, Long> {

    boolean existsByMatricule(String matricule);

    @Query("""
           SELECT c FROM Conducteur c
           WHERE LOWER(c.matricule) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(c.nom)       LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(c.prenom)    LIKE LOWER(CONCAT('%', :q, '%'))
           """)
    List<Conducteur> rechercher(@Param("q") String q);
}