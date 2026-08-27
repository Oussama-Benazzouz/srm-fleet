package ma.srm.fleet.referentiel.vehicule;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehiculeRepository
    extends JpaRepository<Vehicule, Long>, JpaSpecificationExecutor<Vehicule> {

    boolean existsByImmatriculation(String immatriculation);

    // --- Agrégations tableau de bord (scope province : prov null = national) ---

    @Query("select v.statut, count(v) from Vehicule v where (:prov is null or v.province.id = :prov) group by v.statut")
    List<Object[]> compterParStatut(@Param("prov") Long prov);

    @Query("select v.modeAcquisition, count(v) from Vehicule v "
        + "where (:prov is null or v.province.id = :prov) group by v.modeAcquisition")
    List<Object[]> compterParAcquisition(@Param("prov") Long prov);

    @Query("select v.province.libelle, count(v) from Vehicule v "
        + "where (:prov is null or v.province.id = :prov) group by v.province.libelle order by v.province.libelle")
    List<Object[]> compterParProvince(@Param("prov") Long prov);
}
