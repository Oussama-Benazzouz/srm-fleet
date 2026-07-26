package ma.srm.fleet.referentiel.province.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de requete pour la creation et la modification d'une province.
 *
 * Un seul champ, obligatoire : le libelle (nom de la province). Structure
 * volontairement minimale, coherente avec le referentiel geographique fixe
 * de la region Souss-Massa (peu de champs, peu d'evolution attendue).
 */
public record ProvinceRequest(@NotBlank String libelle) {}
