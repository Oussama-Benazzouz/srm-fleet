package ma.srm.fleet.conducteur.conducteur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConducteurRepository
    extends JpaRepository<Conducteur, Long>, JpaSpecificationExecutor<Conducteur> {

    boolean existsByMatricule(String matricule);

    /** Tableau de bord : nombre de conducteurs (scope province ; prov null = national). */
    @Query("select count(c) from Conducteur c where (:prov is null or c.province.id = :prov)")
    long compter(@Param("prov") Long prov);
}
