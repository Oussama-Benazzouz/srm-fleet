package ma.srm.fleet.export;

import ma.srm.fleet.affectation.StatutAffectation;
import ma.srm.fleet.carte.cartecarburant.StatutCarte;
import ma.srm.fleet.referentiel.vehicule.StatutVehicule;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exports par module (RF-25) au format CSV ou PDF, reflétant les filtres passés (RF-26) et
 * cloisonnés par province via les services réutilisés.
 */
@RestController
@RequestMapping("/api/v1/exports")
public class ExportController {

    private final ExportService service;

    public ExportController(ExportService service) { this.service = service; }

    @GetMapping("/vehicules")
    public ResponseEntity<byte[]> vehicules(@AuthenticationPrincipal UserPrincipal user,
                                            @RequestParam(defaultValue = "CSV") ExportFormat format,
                                            @RequestParam(required = false) String immatriculation,
                                            @RequestParam(required = false) String type,
                                            @RequestParam(required = false) StatutVehicule statut) {
        return reponse(service.vehicules(user, format, immatriculation, type, statut));
    }

    @GetMapping("/affectations")
    public ResponseEntity<byte[]> affectations(@AuthenticationPrincipal UserPrincipal user,
                                               @RequestParam(defaultValue = "CSV") ExportFormat format,
                                               @RequestParam(required = false) StatutAffectation statut,
                                               @RequestParam(required = false) Long vehiculeId,
                                               @RequestParam(required = false) Long conducteurId) {
        return reponse(service.affectations(user, format, statut, vehiculeId, conducteurId));
    }

    @GetMapping("/cartes")
    public ResponseEntity<byte[]> cartes(@AuthenticationPrincipal UserPrincipal user,
                                         @RequestParam(defaultValue = "CSV") ExportFormat format,
                                         @RequestParam(required = false) StatutCarte statut,
                                         @RequestParam(required = false) Long affectationId) {
        return reponse(service.cartes(user, format, statut, affectationId));
    }

    private ResponseEntity<byte[]> reponse(ExportFichier fichier) {
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(fichier.contentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fichier.nomFichier() + "\"")
            .body(fichier.contenu());
    }
}
