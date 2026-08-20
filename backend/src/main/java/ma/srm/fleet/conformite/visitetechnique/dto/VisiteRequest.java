package ma.srm.fleet.conformite.visitetechnique.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * RF-19 : enregistrer une visite technique.
 * Le conducteur accompagnateur n'est pas saisi ici : il est dérivé de l'affectation
 * couvrant {@code dateVisite} (RG11), garantissant qu'il correspond bien au titulaire.
 */
public record VisiteRequest(
    @NotNull Long vehiculeId,
    @NotNull LocalDate dateVisite,
    @NotNull LocalDate dateEcheance,
    @NotBlank String nomCompagnie) {}
