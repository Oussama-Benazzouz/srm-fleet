package ma.srm.fleet.affectation.dto;

import ma.srm.fleet.affectation.StatutAffectation;

public record AffectationFilter(StatutAffectation statut, Long vehiculeId, Long conducteurId) {}
