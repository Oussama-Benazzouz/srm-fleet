package ma.srm.fleet.conformite.visitetechnique.dto;

import java.time.LocalDate;

public record VisiteResponse(
    Long id,
    Long vehiculeId,
    String immatriculation,
    Long provinceId,
    String provinceLibelle,
    LocalDate dateVisite,
    LocalDate dateEcheance,
    String nomCompagnie,
    Long conducteurId,
    String conducteurNomComplet,
    String matriculeConducteur) {}
