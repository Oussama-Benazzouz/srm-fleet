package ma.srm.fleet.conformite.sinistre;

import jakarta.validation.Valid;
import java.util.List;
import ma.srm.fleet.conformite.sinistre.dto.ChangerStatutRequest;
import ma.srm.fleet.conformite.sinistre.dto.SinistreRequest;
import ma.srm.fleet.conformite.sinistre.dto.SinistreResponse;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sinistres")
public class SinistreController {

    private final SinistreService service;

    public SinistreController(SinistreService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR_CENTRAL','RESPONSABLE_PROVINCIAL')")
    public SinistreResponse creer(@Valid @RequestBody SinistreRequest req,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        return service.creer(req, principal);
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR_CENTRAL','RESPONSABLE_PROVINCIAL')")
    public SinistreResponse changerStatut(@PathVariable Long id, @Valid @RequestBody ChangerStatutRequest req,
                                          @AuthenticationPrincipal UserPrincipal principal) {
        return service.changerStatut(id, req.statut(), principal);
    }

    @GetMapping
    public List<SinistreResponse> lister(@RequestParam(required = false) StatutSinistre statut,
                                         @RequestParam(required = false) Long vehiculeId,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        return service.lister(statut, vehiculeId, principal);
    }

    @GetMapping("/{id}")
    public SinistreResponse parId(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return service.parId(id, principal);
    }
}
