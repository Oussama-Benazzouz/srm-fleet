package ma.srm.fleet.referentiel.fournisseur.dto;

import ma.srm.fleet.referentiel.fournisseur.TypeFournisseur;

public record FournisseurResponse(
    Long id, String raisonSociale, TypeFournisseur type, String contact, String adresseFacturation) {}
