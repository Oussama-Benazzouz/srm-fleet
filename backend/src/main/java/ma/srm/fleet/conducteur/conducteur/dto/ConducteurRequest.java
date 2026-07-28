package ma.srm.fleet.conducteur.conducteur.dto;

import jakarta.validation.constraints.NotBlank;

public record ConducteurRequest(
        @NotBlank String matricule,
        @NotBlank String nom,
        @NotBlank String prenom,
        String fonction,
        String groupeSanguin,
        Long departementId) {}