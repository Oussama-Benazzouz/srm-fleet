package ma.srm.fleet.conformite.assurance;

import jakarta.validation.Valid;
import java.util.List;
import ma.srm.fleet.conformite.assurance.dto.AssuranceRequest;
import ma.srm.fleet.conformite.assurance.dto.AssuranceResponse;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assurances")
public class AssuranceController {

    private final AssuranceService service;

    public AssuranceController(AssuranceService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR_CENTRAL','RESPONSABLE_PROVINCIAL')")
    public AssuranceResponse creer(@Valid @RequestBody AssuranceRequest req,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        return service.creer(req, principal);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR_CENTRAL','RESPONSABLE_PROVINCIAL')")
    public AssuranceResponse modifier(@PathVariable Long id, @Valid @RequestBody AssuranceRequest req,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        return service.modifier(id, req, principal);
    }

    @GetMapping
    public List<AssuranceResponse> lister(@RequestParam(required = false) Long vehiculeId,
                                          @AuthenticationPrincipal UserPrincipal principal) {
        return service.lister(vehiculeId, principal);
    }

    @GetMapping("/{id}")
    public AssuranceResponse parId(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return service.parId(id, principal);
    }
}
