package ma.srm.fleet.referentiel.fournisseur;

import jakarta.validation.Valid;
import java.util.List;
import ma.srm.fleet.referentiel.fournisseur.dto.FournisseurRequest;
import ma.srm.fleet.referentiel.fournisseur.dto.FournisseurResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fournisseurs")
public class FournisseurController {

    private final FournisseurService service;

    public FournisseurController(FournisseurService service) { this.service = service; }

    @GetMapping
    public List<FournisseurResponse> lister() { return service.lister(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public FournisseurResponse creer(@Valid @RequestBody FournisseurRequest req) { return service.creer(req); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public FournisseurResponse modifier(@PathVariable Long id, @Valid @RequestBody FournisseurRequest req) {
        return service.modifier(id, req);
    }
}
