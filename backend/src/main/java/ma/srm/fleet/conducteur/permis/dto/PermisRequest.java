package ma.srm.fleet.conducteur.permis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PermisRequest(
        @NotBlank String typePermis,
        @NotNull LocalDate dateFinValidite) {}