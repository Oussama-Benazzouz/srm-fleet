package ma.srm.fleet.conducteur.conducteur;

import jakarta.validation.Valid;
import java.util.List;
import ma.srm.fleet.conducteur.conducteur.dto.ConducteurFilter;
import ma.srm.fleet.conducteur.conducteur.dto.ConducteurRequest;
import ma.srm.fleet.conducteur.conducteur.dto.ConducteurResponse;
import ma.srm.fleet.conducteur.permis.dto.PermisRequest;
import ma.srm.fleet.conducteur.permis.dto.PermisResponse;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/conducteurs")
public class ConducteurController {

    private final ConducteurService service;

    public ConducteurController(ConducteurService service) { this.service = service; }

    // Lecture ouverte aux deux rôles ; la portée provinciale est appliquée dans le service.
    @GetMapping
    public List<ConducteurResponse> lister(
        @RequestParam(required = false) String recherche,
        @RequestParam(required = false) Long provinceId,
        @RequestParam(required = false) Long departementId,
        @AuthenticationPrincipal UserPrincipal principal) {
        return service.lister(new ConducteurFilter(recherche, provinceId, departementId), principal);
    }

    @GetMapping("/{id}")
    public ConducteurResponse parId(@PathVariable Long id,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return service.parId(id, principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public ConducteurResponse creer(@Valid @RequestBody ConducteurRequest req) {
        return service.creer(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public ConducteurResponse modifier(@PathVariable Long id, @Valid @RequestBody ConducteurRequest req) {
        return service.modifier(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }

    @PostMapping("/{id}/permis")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public PermisResponse ajouterPermis(@PathVariable Long id, @Valid @RequestBody PermisRequest req) {
        return service.ajouterPermis(id, req);
    }

    @DeleteMapping("/{id}/permis/{permisId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public void supprimerPermis(@PathVariable Long id, @PathVariable Long permisId) {
        service.supprimerPermis(id, permisId);
    }
}
