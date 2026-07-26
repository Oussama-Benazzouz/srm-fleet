package ma.srm.fleet.referentiel.province;

import jakarta.validation.Valid;
import java.util.List;
import ma.srm.fleet.referentiel.province.dto.ProvinceRequest;
import ma.srm.fleet.referentiel.province.dto.ProvinceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controleur REST exposant le referentiel des provinces (RF-28).
 *
 * A la difference de UtilisateurController (ou @PreAuthorize etait pose
 * au niveau classe, toute la ressource etant fermee), ici la restriction
 * est posee methode par methode : la lecture (lister) reste accessible a
 * tout utilisateur authentifie, seule l'ecriture (creer, modifier) est
 * reservee a l'Administrateur Central. C'est le cas d'usage exact anticipe
 * plus tot en commentant UtilisateurController.
 */
@RestController
@RequestMapping("/api/v1/provinces")
public class ProvinceController {

    private final ProvinceService service;

    public ProvinceController(ProvinceService service) {
        this.service = service;
    }

    /**
     * Liste toutes les provinces.
     * GET /api/v1/provinces
     *
     * Lecture ouverte a tout utilisateur authentifie (listes déroulantes).
     * Necessaire par exemple pour qu'un Responsable Provincial puisse
     * afficher un selecteur de province lors de la creation d'un vehicule
     * ou d'un compte, meme s'il ne peut pas modifier ce referentiel
     * lui-meme.
     */
    @GetMapping
    public List<ProvinceResponse> lister() {
        return service.lister();
    }

    /**
     * Cree une nouvelle province.
     * POST /api/v1/provinces
     *
     * @PreAuthorize propre a cette methode (pas au niveau classe) :
     * seule l'ecriture est restreinte, coherent avec le commentaire
     * explicite sur GetMapping ci-dessus.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")   // RF-28 : écriture réservée à l'admin central
    public ProvinceResponse creer(@Valid @RequestBody ProvinceRequest req) {
        return service.creer(req);
    }

    /**
     * Modifie le libelle d'une province existante.
     * PUT /api/v1/provinces/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public ProvinceResponse modifier(@PathVariable Long id, @Valid @RequestBody ProvinceRequest req) {
        return service.modifier(id, req);
    }
}
