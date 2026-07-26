package ma.srm.fleet.referentiel.departement;

import jakarta.validation.Valid;
import java.util.List;
import ma.srm.fleet.referentiel.departement.dto.DepartementRequest;
import ma.srm.fleet.referentiel.departement.dto.DepartementResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controleur REST exposant le referentiel des departements (RF-28).
 *
 * Meme patron que ProvinceController : lecture ouverte a tout utilisateur
 * authentifie (listes deroulantes, ex. rattachement d'un vehicule ou d'un
 * conducteur a un departement), ecriture reservee a l'Administrateur
 * Central via @PreAuthorize methode par methode.
 */
@RestController
@RequestMapping("/api/v1/departements")
public class DepartementController {

    private final DepartementService service;

    public DepartementController(DepartementService service) {
        this.service = service;
    }

    /**
     * Liste tous les departements.
     * GET /api/v1/departements
     *
     * Lecture ouverte à tout utilisateur authentifié (listes déroulantes).
     */
    @GetMapping
    public List<DepartementResponse> lister() {
        return service.lister();
    }

    /**
     * Cree un nouveau departement.
     * POST /api/v1/departements
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")   // RF-28 : écriture réservée à l'admin central
    public DepartementResponse creer(@Valid @RequestBody DepartementRequest req) {
        return service.creer(req);
    }

    /**
     * Modifie le libelle d'un departement existant.
     * PUT /api/v1/departements/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public DepartementResponse modifier(@PathVariable Long id, @Valid @RequestBody DepartementRequest req) {
        return service.modifier(id, req);
    }
}
