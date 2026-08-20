package ma.srm.fleet.conformite.sinistre.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** RF-23 : déclarer un sinistre sur un véhicule. L'affectation active à la date est résolue côté serveur (RG7). */
public record SinistreRequest(
    @NotNull Long vehiculeId,
    @NotNull LocalDate dateSinistre,
    String description) {}
