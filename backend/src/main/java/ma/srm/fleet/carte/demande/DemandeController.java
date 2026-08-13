package ma.srm.fleet.carte.demande;

import jakarta.validation.Valid;
import java.util.List;
import ma.srm.fleet.carte.demande.dto.DemandeRequest;
import ma.srm.fleet.carte.demande.dto.DemandeResponse;
import ma.srm.fleet.carte.demande.dto.ValiderRequest;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/demandes")
public class DemandeController {

    private final DemandeService service;

    public DemandeController(DemandeService service) { this.service = service; }

    @GetMapping
    public List<DemandeResponse> lister(@RequestParam(required = false) StatutDemandeCarte statut,
                                        @RequestParam(required = false) Long affectationId,
                                        @AuthenticationPrincipal UserPrincipal principal) {
        return service.lister(statut, affectationId, principal);
    }

    @GetMapping("/{id}")
    public DemandeResponse parId(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return service.parId(id, principal);
    }

    // Création : les deux rôles (scopée dans le service)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR_CENTRAL','RESPONSABLE_PROVINCIAL')")
    public DemandeResponse creer(@Valid @RequestBody DemandeRequest req,
                                 @AuthenticationPrincipal UserPrincipal principal) {
        return service.creer(req, principal);
    }

    // Traitement : admin central uniquement
    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public DemandeResponse valider(@PathVariable Long id, @Valid @RequestBody ValiderRequest req,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        return service.valider(id, req, principal);
    }

    @PatchMapping("/{id}/refuser")
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public DemandeResponse refuser(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return service.refuser(id, principal);
    }
}
