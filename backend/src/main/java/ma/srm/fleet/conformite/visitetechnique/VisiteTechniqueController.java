package ma.srm.fleet.conformite.visitetechnique;

import jakarta.validation.Valid;
import java.util.List;
import ma.srm.fleet.conformite.visitetechnique.dto.VisiteRequest;
import ma.srm.fleet.conformite.visitetechnique.dto.VisiteResponse;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/visites-techniques")
public class VisiteTechniqueController {

    private final VisiteTechniqueService service;

    public VisiteTechniqueController(VisiteTechniqueService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR_CENTRAL','RESPONSABLE_PROVINCIAL')")
    public VisiteResponse creer(@Valid @RequestBody VisiteRequest req,
                                @AuthenticationPrincipal UserPrincipal principal) {
        return service.creer(req, principal);
    }

    @GetMapping
    public List<VisiteResponse> lister(@RequestParam(required = false) Long vehiculeId,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        return service.lister(vehiculeId, principal);
    }

    @GetMapping("/{id}")
    public VisiteResponse parId(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return service.parId(id, principal);
    }
}
