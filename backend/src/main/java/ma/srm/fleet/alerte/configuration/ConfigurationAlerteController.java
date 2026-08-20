package ma.srm.fleet.alerte.configuration;

import jakarta.validation.Valid;
import java.util.List;
import ma.srm.fleet.alerte.configuration.dto.ConfigurationAlerteResponse;
import ma.srm.fleet.alerte.configuration.dto.SeuilRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** RF-20 : administration des seuils d'alerte, réservée à l'Administrateur Central. */
@RestController
@RequestMapping("/api/v1/configuration-alertes")
@PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
public class ConfigurationAlerteController {

    private final ConfigurationAlerteService service;

    public ConfigurationAlerteController(ConfigurationAlerteService service) { this.service = service; }

    @GetMapping
    public List<ConfigurationAlerteResponse> lister() {
        return service.lister();
    }

    @PatchMapping("/{typeSource}")
    public ConfigurationAlerteResponse modifierSeuil(@PathVariable TypeSourceAlerte typeSource,
                                                     @Valid @RequestBody SeuilRequest req) {
        return service.modifierSeuil(typeSource, req.seuilJours());
    }
}
