package ma.srm.fleet.admin.utilisateur;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository Spring Data JPA pour l'entite Utilisateur.
 *
 * Fournit deja gratuitement, via JpaRepository, les operations CRUD de base
 * (save, findById, findAll, delete...) sans code supplementaire. Seules les
 * methodes de recherche specifiques au metier (login, unicite) sont
 * declarees ici.
 *
 * Aucune implementation a ecrire : Spring Data genere automatiquement la
 * requete a partir du nom de la methode (derivation de requete).
 */
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    /**
     * Recherche un utilisateur par son identifiant de connexion.
     *
     * Utilise notamment lors de l'authentification pour retrouver
     * le compte correspondant au login saisi.
     *
     * Optional : traduit explicitement le fait qu'aucun utilisateur ne
     * correspond peut-etre au login recherche, sans avoir a tester null.
     *
     * @param login l'identifiant de connexion recherche
     * @return l'utilisateur correspondant, ou Optional.empty() si aucun
     */
    Optional<Utilisateur> findByLogin(String login);

    /**
     * Verifie si un login est deja utilise par un compte existant.
     *
     * Plus efficace qu'un findByLogin(...).isPresent() : cette methode
     * genere une requete de type EXISTS, qui ne charge pas l'entite
     * complete en memoire, contrairement a une recherche classique.
     *
     * Utile avant la creation d'un compte (RF-27) pour garantir l'unicite
     * du login sans dependre uniquement de la contrainte UNIQUE en base.
     *
     * @param login l'identifiant de connexion a verifier
     * @return true si ce login est deja pris, false sinon
     */
    boolean existsByLogin(String login);

    /**
     * Verifie si une adresse email est deja utilisee par un compte existant.
     *
     * Meme logique que existsByLogin : verification rapide d'unicite avant
     * creation ou modification d'un compte utilisateur (RG14 : seul
     * l'Administrateur Central peut creer/modifier un compte).
     *
     * @param email l'adresse email a verifier
     * @return true si cet email est deja pris, false sinon
     */
    boolean existsByEmail(String email);
}
