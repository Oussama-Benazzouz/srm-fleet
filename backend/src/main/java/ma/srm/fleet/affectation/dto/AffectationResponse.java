package ma.srm.fleet.affectation.dto;

import java.time.LocalDate;
import ma.srm.fleet.affectation.StatutAffectation;

public record AffectationResponse(
    Long id,
    Long vehiculeId,
    String immatriculation,
    Long conducteurId,
    String conducteurNomComplet,
    String matriculeConducteur,
    Long provinceId,
    String provinceLibelle,
    LocalDate dateDebut,
    LocalDate dateFin,
    StatutAffectation statut,
    String motif) {}
