package ma.srm.fleet.referentiel.departement.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de requete pour la creation et la modification d'un departement.
 *
 * Meme structure minimale que ProvinceRequest : un seul champ obligatoire,
 * coherent avec le referentiel organisationnel fixe (ex. DSI) mentionne
 * dans le cahier des charges.
 */
public record DepartementRequest(@NotBlank String libelle) {}
