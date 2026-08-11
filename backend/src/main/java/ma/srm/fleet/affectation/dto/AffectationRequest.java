package ma.srm.fleet.affectation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AffectationRequest(
    @NotNull Long vehiculeId,
    @NotNull Long conducteurId,
    LocalDate dateDebut,            // défaut : aujourd'hui si null
    @Size(max = 255) String motif) {}
