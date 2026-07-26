package ma.srm.fleet.admin.utilisateur;

import jakarta.validation.Valid;
import java.util.List;
import ma.srm.fleet.admin.utilisateur.dto.UtilisateurRequest;
import ma.srm.fleet.admin.utilisateur.dto.UtilisateurResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controleur REST exposant la gestion des comptes utilisateurs (RF-27).
 *
 * @PreAuthorize au niveau de la classe (et non repete sur chaque methode) :
 * choix delibere ici, car RG14 est une regle absolue et sans exception
 * pour l'ensemble de la ressource /utilisateurs — aucun endpoint de ce
 * controleur, pas meme la lecture (lister/parId), n'est accessible a un
 * Responsable Provincial. Poser l'annotation une seule fois au niveau
 * classe evite le risque d'oubli sur un futur endpoint ajoute ici, qui
 * serait sinon accessible par defaut a tout utilisateur authentifie.
 *
 * A comparer avec un controleur ou seules certaines actions sont
 * restreintes (ex. futur VehiculeController, ou la lecture pourrait
 * rester ouverte au Responsable Provincial mais pas la creation RG12) :
 * dans ce cas-la, @PreAuthorize devra etre pose methode par methode
 * plutot qu'au niveau classe.
 */
@RestController
@RequestMapping("/api/v1/utilisateurs")
@PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")   // RF-27 : toute la ressource réservée à l'admin central
public class UtilisateurController {

    private final UtilisateurService service;

    // Injection par constructeur (convention du projet)
    public UtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    /**
     * Liste tous les comptes utilisateurs.
     * GET /api/v1/utilisateurs
     */
    @GetMapping
    public List<UtilisateurResponse> lister() {
        return service.lister();
    }

    /**
     * Recupere un compte utilisateur par son identifiant.
     * GET /api/v1/utilisateurs/{id}
     */
    @GetMapping("/{id}")
    public UtilisateurResponse parId(@PathVariable Long id) {
        return service.parId(id);
    }

    /**
     * Cree un nouveau compte utilisateur.
     * POST /api/v1/utilisateurs
     *
     * @Valid declenche la validation Bean Validation du DTO (nom, prenom,
     * login, email, role obligatoires) avant meme d'atteindre le service ;
     * une requete invalide est rejetee automatiquement par le
     * GlobalExceptionHandler (400, RFC 7807), sans executer la moindre
     * logique metier.
     *
     * @ResponseStatus(CREATED) : renvoie explicitement 201 plutot que le
     * 200 par defaut de Spring MVC, conforme a la semantique REST d'une
     * creation de ressource reussie.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UtilisateurResponse creer(@Valid @RequestBody UtilisateurRequest req) {
        return service.creer(req);
    }

    /**
     * Modifie un compte utilisateur existant.
     * PUT /api/v1/utilisateurs/{id}
     *
     * Meme validation qu'a la creation, mais motDePasse reste facultatif
     * (non impose par Bean Validation, geree manuellement dans le service,
     * cf. UtilisateurService.modifier).
     */
    @PutMapping("/{id}")
    public UtilisateurResponse modifier(@PathVariable Long id, @Valid @RequestBody UtilisateurRequest req) {
        return service.modifier(id, req);
    }

    /**
     * Desactive un compte utilisateur.
     * PATCH /api/v1/utilisateurs/{id}/desactiver
     *
     * PATCH plutot que DELETE : coherent avec RF-27 (jamais de suppression
     * physique d'un compte, uniquement un changement de statut) — DELETE
     * suggererait a tort une suppression definitive de la ressource.
     */
    @PatchMapping("/{id}/desactiver")
    public UtilisateurResponse desactiver(@PathVariable Long id) {
        return service.changerStatut(id, StatutCompte.DESACTIVE);
    }

    /**
     * Reactive un compte utilisateur precedemment desactive.
     * PATCH /api/v1/utilisateurs/{id}/activer
     *
     * Symetrique de desactiver() : meme mecanisme (changerStatut), statut
     * cible different. Permet de revenir en arriere sans recreer le
     * compte (et donc sans perdre son historique).
     */
    @PatchMapping("/{id}/activer")
    public UtilisateurResponse activer(@PathVariable Long id) {
        return service.changerStatut(id, StatutCompte.ACTIF);
    }
}
