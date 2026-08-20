package ma.srm.fleet.conformite.assurance.dto;

import java.time.LocalDate;

public record AssuranceResponse(
    Long id,
    Long vehiculeId,
    String immatriculation,
    Long provinceId,
    String provinceLibelle,
    String nomCompagnie,
    LocalDate dateDebut,
    LocalDate dateEcheance) {}
