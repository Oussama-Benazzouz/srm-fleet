package ma.srm.fleet.conformite.sinistre.dto;

import java.time.LocalDate;
import ma.srm.fleet.conformite.sinistre.StatutSinistre;

public record SinistreResponse(
    Long id,
    Long vehiculeId,
    String immatriculation,
    Long provinceId,
    String provinceLibelle,
    LocalDate dateSinistre,
    String description,
    StatutSinistre statut,
    Long affectationId,
    Long conducteurId,
    String conducteurNomComplet) {}
