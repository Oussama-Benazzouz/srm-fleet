package ma.srm.fleet.affectation;

import jakarta.validation.Valid;
import java.util.List;
import ma.srm.fleet.affectation.dto.AffectationRequest;
import ma.srm.fleet.affectation.dto.AffectationResponse;
import ma.srm.fleet.affectation.dto.ClotureRequest;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/affectations")
public class AffectationController {

    private final AffectationService service;

    public AffectationController(AffectationService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR_CENTRAL','RESPONSABLE_PROVINCIAL')")
    public AffectationResponse creer(@Valid @RequestBody AffectationRequest req,
                                     @AuthenticationPrincipal UserPrincipal principal) {
        return service.creer(req, principal);
    }

    @PatchMapping("/{id}/cloturer")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR_CENTRAL','RESPONSABLE_PROVINCIAL')")
    public AffectationResponse cloturer(@PathVariable Long id,
                                        @RequestBody(required = false) ClotureRequest req,
                                        @AuthenticationPrincipal UserPrincipal principal) {
        return service.cloturer(id, req != null ? req : new ClotureRequest(null), principal);
    }

    @GetMapping
    public java.util.List<AffectationResponse> lister(
        @RequestParam(required = false) ma.srm.fleet.affectation.StatutAffectation statut,
        @RequestParam(required = false) Long vehiculeId,
        @RequestParam(required = false) Long conducteurId,
        @AuthenticationPrincipal UserPrincipal principal) {
        return service.lister(
            new ma.srm.fleet.affectation.dto.AffectationFilter(statut, vehiculeId, conducteurId), principal);
    }

    @GetMapping("/historique/vehicule/{vehiculeId}")
    public List<AffectationResponse> historiqueVehicule(@PathVariable Long vehiculeId,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        return service.historiqueVehicule(vehiculeId, principal);
    }

    @GetMapping("/historique/conducteur/{conducteurId}")
    public List<AffectationResponse> historiqueConducteur(@PathVariable Long conducteurId,
                                                          @AuthenticationPrincipal UserPrincipal principal) {
        return service.historiqueConducteur(conducteurId, principal);
    }
}
