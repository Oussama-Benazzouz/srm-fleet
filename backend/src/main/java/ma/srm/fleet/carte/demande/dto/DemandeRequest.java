package ma.srm.fleet.carte.demande.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DemandeRequest(
    @NotNull Long affectationId,
    @Size(max = 255) String motif) {}
