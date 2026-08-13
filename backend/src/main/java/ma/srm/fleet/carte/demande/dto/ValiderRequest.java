package ma.srm.fleet.carte.demande.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ValiderRequest(
    @NotBlank String numeroCarte,
    @NotNull @Positive BigDecimal plafondMontant,
    @NotNull Long fournisseurId) {}
