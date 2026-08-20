package ma.srm.fleet.alerte.configuration.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** RF-20 : ajuster le seuil (en jours) d'un type d'échéance surveillé. */
public record SeuilRequest(@NotNull @Positive Integer seuilJours) {}
