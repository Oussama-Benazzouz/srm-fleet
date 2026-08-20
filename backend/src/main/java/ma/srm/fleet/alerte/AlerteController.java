package ma.srm.fleet.alerte;

import java.util.List;
import ma.srm.fleet.alerte.dto.AlerteResponse;
import ma.srm.fleet.alerte.dto.GenerationResult;
import ma.srm.fleet.alerte.engine.AlerteGenerationService;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alertes")
public class AlerteController {

    private final AlerteService service;
    private final AlerteGenerationService generationService;

    public AlerteController(AlerteService service, AlerteGenerationService generationService) {
        this.service = service;
        this.generationService = generationService;
    }

    /** RF-22 : tableau de bord des alertes actives, triées par urgence, scopées par province. */
    @GetMapping
    public List<AlerteResponse> lister(@AuthenticationPrincipal UserPrincipal principal) {
        return service.lister(principal);
    }

    @PatchMapping("/{id}/lue")
    public AlerteResponse marquerLue(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return service.marquerLue(id, principal);
    }

    /**
     * Déclenchement manuel du moteur (Administrateur Central), utile pour la démonstration et les
     * tests sans attendre l'exécution planifiée quotidienne.
     */
    @PostMapping("/generer")
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public GenerationResult generer() {
        return new GenerationResult(generationService.genererToutes());
    }
}
