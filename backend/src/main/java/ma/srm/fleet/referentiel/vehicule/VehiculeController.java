package ma.srm.fleet.referentiel.vehicule;

import jakarta.validation.Valid;
import java.util.List;
import ma.srm.fleet.referentiel.vehicule.dto.VehiculeRequest;
import ma.srm.fleet.referentiel.vehicule.dto.VehiculeResponse;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vehicules")
public class VehiculeController {

    private final VehiculeService service;

    public VehiculeController(VehiculeService service) { this.service = service; }

    @GetMapping
    public List<VehiculeResponse> rechercher(
        @AuthenticationPrincipal UserPrincipal user,
        @RequestParam(required = false) String immatriculation,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) StatutVehicule statut) {
        return service.rechercher(user, immatriculation, type, statut);
    }

    @GetMapping("/{id}")
    public VehiculeResponse parId(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {
        return service.parId(user, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public VehiculeResponse creer(@Valid @RequestBody VehiculeRequest req) { return service.creer(req); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public VehiculeResponse modifier(@PathVariable Long id, @Valid @RequestBody VehiculeRequest req) {
        return service.modifier(id, req);
    }
}
