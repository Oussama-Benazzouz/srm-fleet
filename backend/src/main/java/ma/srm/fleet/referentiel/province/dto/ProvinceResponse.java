package ma.srm.fleet.referentiel.province.dto;

/**
 * DTO de reponse pour une province.
 *
 * Aucune donnee sensible a filtrer ici (contrairement a
 * UtilisateurResponse) : le DTO expose directement les memes champs que
 * l'entite, la conversion sert surtout a decoupler le contrat d'API du
 * modele de persistance (evite d'exposer directement Province, qui pourrait
 * evoluer avec des relations supplementaires — ex. vehicules rattaches).
 */
public record ProvinceResponse(Long id, String libelle) {}
