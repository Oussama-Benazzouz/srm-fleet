package ma.srm.fleet.conformite.assurance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** RF-18 : enregistrer une échéance d'assurance pour un véhicule. */
public record AssuranceRequest(
    @NotNull Long vehiculeId,
    @NotBlank String nomCompagnie,
    @NotNull LocalDate dateDebut,
    @NotNull LocalDate dateEcheance) {}
