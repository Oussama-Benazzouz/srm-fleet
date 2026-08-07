package ma.srm.fleet.conducteur.conducteur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConducteurRequest(
        @NotBlank String matricule,
        @NotBlank String nom,
        @NotBlank String prenom,
        String fonction,
        String groupeSanguin,
        @NotNull Long provinceId,
        @NotNull Long departementId) {}
