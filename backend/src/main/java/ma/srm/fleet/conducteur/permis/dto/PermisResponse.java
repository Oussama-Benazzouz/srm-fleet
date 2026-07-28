package ma.srm.fleet.conducteur.permis.dto;

import java.time.LocalDate;

public record PermisResponse(Long id, String typePermis, LocalDate dateFinValidite) {}