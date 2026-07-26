package ma.srm.fleet.referentiel.province;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository Spring Data JPA pour l'entite Province.
 *
 * Herite gratuitement de JpaRepository les operations CRUD de base
 * (save, findById, findAll, delete...). Une seule methode specifique au
 * metier est necessaire ici.
 */
public interface ProvinceRepository extends JpaRepository<Province, Long> {

    /**
     * Verifie si une province portant ce libelle existe deja.
     *
     * Genere une requete de type EXISTS (plus efficace qu'un chargement
     * complet de l'entite juste pour tester sa presence). Utile pour
     * empecher la creation de doublons dans le referentiel des provinces,
     * gere via RF-28 (administration des provinces et departements sans
     * intervention en base).
     *
     * @param libelle le nom de la province a verifier
     * @return true si une province porte deja ce libelle, false sinon
     */
    boolean existsByLibelle(String libelle);
}
