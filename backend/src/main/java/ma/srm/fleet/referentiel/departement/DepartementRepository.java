package ma.srm.fleet.referentiel.departement;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository Spring Data JPA pour l'entité Departement.
 *
 * Les operations CRUD de base sont deja fournies par JpaRepository
 * , seule la verification d'unicité du libelle est ajoutée ici.
 */
public interface DepartementRepository extends JpaRepository<Departement, Long> {

    /**
     * Verifie si un departement portant ce libelle existe deja.
     *
     * Meme usage que ProvinceRepository.existsByLibelle : evite les
     * doublons dans le referentiel des departements (RF-28), sans devoir
     * charger l'entite complete pour le test.
     *
     * @param libelle le nom du departement a verifier
     * @return true si un departement porte deja ce libelle, false sinon
     */
    boolean existsByLibelle(String libelle);
}
