package ma.srm.fleet.conducteur.conducteur.dto;

/**
 * Critères de filtrage de la liste des conducteurs. Chaque champ est optionnel
 * (null = critère ignoré). Ajouter un futur critère = ajouter un champ ici + une
 * méthode dans ConducteurSpecifications, sans toucher au reste.
 */
public record ConducteurFilter(String recherche, Long provinceId, Long departementId) {}
