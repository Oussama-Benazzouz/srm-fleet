package ma.srm.fleet.carte.cartecarburant;

import java.util.List;
import ma.srm.fleet.carte.cartecarburant.dto.CarteResponse;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cartes")
public class CarteController {

    private final CarteService service;

    public CarteController(CarteService service) { this.service = service; }

    @GetMapping
    public List<CarteResponse> lister(@RequestParam(required = false) StatutCarte statut,
                                      @RequestParam(required = false) Long affectationId,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        return service.lister(statut, affectationId, principal);
    }

    @GetMapping("/{id}")
    public CarteResponse parId(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return service.parId(id, principal);
    }

    @PatchMapping("/{id}/cloturer")
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public CarteResponse cloturer(@PathVariable Long id) { return service.cloturer(id); }
}
