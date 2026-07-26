package ma.srm.fleet.admin.utilisateur;

import java.util.List;
import ma.srm.fleet.admin.utilisateur.dto.UtilisateurRequest;
import ma.srm.fleet.admin.utilisateur.dto.UtilisateurResponse;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.province.ProvinceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service metier de gestion des comptes utilisateurs (RF-27).
 *
 * Porte toute la logique de gestion (RG14 : creation/modification/
 * desactivation reservees a l'Administrateur Central — controle applique
 * en amont, via @PreAuthorize sur le controleur, pas ici). Ce service se
 * concentre sur les regles de coherence des donnees elles-memes : unicite
 * du login/email, hachage du mot de passe, coherence role/province.
 *
 * @Transactional au niveau classe : toute methode publique s'execute dans
 * une transaction par defaut ; les methodes de lecture seule la
 * surchargent explicitement en readOnly = true pour l'optimisation
 * (pas de verrouillage inutile, cf. UtilisateurService.lister/parId).
 */
@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository repository;
    private final ProvinceRepository provinceRepository;
    private final PasswordEncoder passwordEncoder;

    // Injection par constructeur (convention du projet)
    public UtilisateurService(UtilisateurRepository repository,
                              ProvinceRepository provinceRepository,
                              PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.provinceRepository = provinceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Liste tous les comptes utilisateurs.
     *
     * @return la liste complete des utilisateurs, au format DTO (jamais
     *         l'entite brute, qui exposerait le mot de passe hache)
     */
    @Transactional(readOnly = true)
    public List<UtilisateurResponse> lister() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Recupere un utilisateur par son identifiant.
     *
     * @param id l'identifiant technique de l'utilisateur recherche
     * @return le DTO de reponse correspondant
     * @throws BusinessException (404) si aucun utilisateur ne correspond
     */
    @Transactional(readOnly = true)
    public UtilisateurResponse parId(Long id) {
        return repository.findById(id).map(this::toResponse)
            .orElseThrow(() -> introuvable(id));
    }

    /**
     * Cree un nouveau compte utilisateur.
     *
     * Trois controles metier avant toute persistance :
     * 1. Mot de passe obligatoire (Bean Validation ne peut pas l'imposer
     *    ici, car motDePasse n'est volontairement pas @NotBlank sur le
     *    DTO — cf. UtilisateurRequest — puisqu'il devient facultatif lors
     *    d'une modification). Ce controle manuel comble cet ecart.
     * 2. Unicite du login (contrainte deja posee en base, mais verifiee
     *    ici en amont pour renvoyer une erreur 409 explicite plutot que
     *    de laisser remonter une exception SQL brute).
     * 3. Unicite de l'email, meme logique.
     *
     * @param req les donnees du compte a creer
     * @return le compte cree, au format DTO
     * @throws BusinessException (400) si le mot de passe est absent ou
     *         si la coherence role/province n'est pas respectee (RG14)
     * @throws BusinessException (409) si le login ou l'email existe deja
     */
    public UtilisateurResponse creer(UtilisateurRequest req) {
        if (req.motDePasse() == null || req.motDePasse().isBlank()) {
            throw new BusinessException("Le mot de passe est obligatoire à la création", HttpStatus.BAD_REQUEST);
        }
        if (repository.existsByLogin(req.login())) {
            throw new BusinessException("Ce login est déjà utilisé", HttpStatus.CONFLICT);
        }
        if (repository.existsByEmail(req.email())) {
            throw new BusinessException("Cet email est déjà utilisé", HttpStatus.CONFLICT);
        }

        Utilisateur u = new Utilisateur();
        u.setNom(req.nom());
        u.setPrenom(req.prenom());
        u.setLogin(req.login());
        u.setEmail(req.email());
        u.setMotDePasse(passwordEncoder.encode(req.motDePasse()));   // hachage BCrypt
        u.setRole(req.role());
        // Statut force a ACTIF a la creation : pas de compte cree deja
        // desactive, coherent avec le cycle de vie attendu (RF-27).
        u.setStatutCompte(StatutCompte.ACTIF);
        u.setProvince(resoudreProvince(req));
        return toResponse(repository.save(u));
    }

    /**
     * Modifie un compte utilisateur existant.
     *
     * Difference cle avec creer() : le mot de passe devient facultatif.
     * S'il est fourni (non vide), il est rehache et remplace l'ancien ;
     * sinon, le mot de passe existant reste inchange — evite de forcer
     * une resaisie a chaque modification de profil.
     *
     * Les controles d'unicite login/email n'excluent le conflit que si la
     * valeur a reellement change (!u.getLogin().equals(req.login())) :
     * sans cette condition, modifier un utilisateur sans toucher a son
     * propre login declencherait systematiquement une fausse erreur de
     * doublon (puisque son login "existe deja" — le sien).
     *
     * Pas de @Transactional(readOnly = true) ici : la methode modifie
     * l'entite. Aucun repository.save() explicite necessaire non plus :
     * l'entite u est deja geree par Hibernate (chargee dans la meme
     * transaction), donc les modifications sont persistees automatiquement
     * au commit (dirty checking).
     *
     * @param id  l'identifiant du compte a modifier
     * @param req les nouvelles donnees du compte
     * @return le compte modifie, au format DTO
     * @throws BusinessException (404) si l'utilisateur n'existe pas
     * @throws BusinessException (409) en cas de conflit login/email
     */
    public UtilisateurResponse modifier(Long id, UtilisateurRequest req) {
        Utilisateur u = repository.findById(id).orElseThrow(() -> introuvable(id));

        if (!u.getLogin().equals(req.login()) && repository.existsByLogin(req.login())) {
            throw new BusinessException("Ce login est déjà utilisé", HttpStatus.CONFLICT);
        }
        if (!u.getEmail().equals(req.email()) && repository.existsByEmail(req.email())) {
            throw new BusinessException("Cet email est déjà utilisé", HttpStatus.CONFLICT);
        }

        u.setNom(req.nom());
        u.setPrenom(req.prenom());
        u.setLogin(req.login());
        u.setEmail(req.email());
        u.setRole(req.role());
        u.setProvince(resoudreProvince(req));
        if (req.motDePasse() != null && !req.motDePasse().isBlank()) {
            u.setMotDePasse(passwordEncoder.encode(req.motDePasse()));
        }
        return toResponse(u);
    }

    /**
     * Change le statut d'un compte (actif/desactive).
     *
     * RF-27 : désactivation (jamais de suppression physique d'un compte).
     * Cette methode ne supprime jamais rien — elle change uniquement le
     * statut, ce qui permet a JwtAuthenticationFilter de refuser l'acces
     * (via UserPrincipal.isEnabled()) sans perdre l'historique associe a
     * ce compte (traçabilite des actions passees).
     *
     * @param id     l'identifiant du compte concerne
     * @param statut le nouveau statut a appliquer
     * @return le compte mis a jour, au format DTO
     * @throws BusinessException (404) si l'utilisateur n'existe pas
     */
    public UtilisateurResponse changerStatut(Long id, StatutCompte statut) {
        Utilisateur u = repository.findById(id).orElseThrow(() -> introuvable(id));
        u.setStatutCompte(statut);
        return toResponse(u);
    }

    /**
     * Determine et valide la province a associer a l'utilisateur, selon
     * son role.
     *
     * Traduit directement la RG14 en code :
     * - un Responsable Provincial DOIT avoir une province (sinon erreur) ;
     * - un Administrateur Central n'en a PAS (portee nationale), la
     *   province est donc laissee a null quel que soit ce qui a ete
     *   envoye par le client — meme si un provinceId etait fourni par
     *   erreur pour un admin, il est ignore ici.
     *
     * @param req la requete contenant le role et l'eventuel provinceId
     * @return la province resolue, ou null pour un administrateur central
     * @throws BusinessException (400) si un responsable provincial n'a
     *         pas de province, ou si la province indiquee n'existe pas
     */
    private Province resoudreProvince(UtilisateurRequest req) {
        if (req.role() == RoleUtilisateur.RESPONSABLE_PROVINCIAL && req.provinceId() == null) {
            throw new BusinessException("Un responsable provincial doit être rattaché à une province",
                HttpStatus.BAD_REQUEST);
        }
        if (req.provinceId() == null) {
            return null;   // administrateur central (RG14)
        }
        return provinceRepository.findById(req.provinceId())
            .orElseThrow(() -> new BusinessException("Province introuvable", HttpStatus.BAD_REQUEST));
    }

    /** Fabrique l'exception standard pour un utilisateur introuvable (404). */
    private BusinessException introuvable(Long id) {
        return new BusinessException("Utilisateur introuvable : " + id, HttpStatus.NOT_FOUND);
    }

    /**
     * Convertit l'entite Utilisateur en DTO de reponse.
     *
     * Point de securite important : le mot de passe hache n'est jamais
     * inclus dans UtilisateurResponse (cf. le DTO lui-meme) — cette
     * methode ne le lit meme pas, aucun risque d'oubli de filtrage.
     */
    private UtilisateurResponse toResponse(Utilisateur u) {
        return new UtilisateurResponse(
            u.getId(), u.getNom(), u.getPrenom(), u.getLogin(), u.getEmail(),
            u.getRole(), u.getStatutCompte(),
            u.getProvince() != null ? u.getProvince().getId() : null);
    }
}
