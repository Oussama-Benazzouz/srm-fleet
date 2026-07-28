package ma.srm.fleet.conducteur.conducteur;

import jakarta.validation.Valid;
import java.util.List;
import ma.srm.fleet.conducteur.conducteur.dto.ConducteurRequest;
import ma.srm.fleet.conducteur.conducteur.dto.ConducteurResponse;
import ma.srm.fleet.conducteur.permis.dto.PermisRequest;
import ma.srm.fleet.conducteur.permis.dto.PermisResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/conducteurs")
public class ConducteurController {

    private final ConducteurService service;

    public ConducteurController(ConducteurService service) { this.service = service; }

    @GetMapping
    public List<ConducteurResponse> lister(@RequestParam(required = false) String recherche) {
        return service.lister(recherche);
    }

    @GetMapping("/{id}")
    public ConducteurResponse parId(@PathVariable Long id) { return service.parId(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR_CENTRAL','RESPONSABLE_PROVINCIAL')")
    public ConducteurResponse creer(@Valid @RequestBody ConducteurRequest req) { return service.creer(req); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR_CENTRAL','RESPONSABLE_PROVINCIAL')")
    public ConducteurResponse modifier(@PathVariable Long id, @Valid @RequestBody ConducteurRequest req) {
        return service.modifier(id, req);
    }

    @PostMapping("/{id}/permis")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR_CENTRAL','RESPONSABLE_PROVINCIAL')")
    public PermisResponse ajouterPermis(@PathVariable Long id, @Valid @RequestBody PermisRequest req) {
        return service.ajouterPermis(id, req);
    }

    @DeleteMapping("/{id}/permis/{permisId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR_CENTRAL','RESPONSABLE_PROVINCIAL')")
    public void supprimerPermis(@PathVariable Long id, @PathVariable Long permisId) {
        service.supprimerPermis(id, permisId);
    }
}