package ma.srm.fleet.referentiel.departement.dto;

/**
 * DTO de reponse pour un departement.
 *
 * Meme logique que ProvinceResponse : decouple le contrat d'API du modele
 * de persistance, sans donnee sensible a filtrer.
 */
public record DepartementResponse(Long id, String libelle) {}
