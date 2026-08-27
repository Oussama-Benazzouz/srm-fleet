package ma.srm.fleet.dashboard;

import ma.srm.fleet.dashboard.dto.DashboardResponse;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tableau-de-bord")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) { this.service = service; }

    /** Statistiques agrégées de tous les modules, scopées par rôle/province. */
    @GetMapping
    public DashboardResponse consulter(@AuthenticationPrincipal UserPrincipal principal) {
        return service.construire(principal);
    }
}
