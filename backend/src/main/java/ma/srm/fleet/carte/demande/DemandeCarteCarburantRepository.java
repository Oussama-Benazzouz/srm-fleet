package ma.srm.fleet.carte.demande;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DemandeCarteCarburantRepository
    extends JpaRepository<DemandeCarteCarburant, Long>, JpaSpecificationExecutor<DemandeCarteCarburant> {

    /** Tableau de bord : demandes en attente de validation (scope province ; prov null = national). */
    @Query("select count(d) from DemandeCarteCarburant d "
        + "where d.statut = ma.srm.fleet.carte.demande.StatutDemandeCarte.EN_ATTENTE "
        + "and (:prov is null or d.affectation.vehicule.province.id = :prov)")
    long compterEnAttente(@Param("prov") Long prov);
}
