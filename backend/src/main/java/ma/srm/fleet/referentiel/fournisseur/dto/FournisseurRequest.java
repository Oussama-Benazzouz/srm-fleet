package ma.srm.fleet.referentiel.fournisseur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ma.srm.fleet.referentiel.fournisseur.TypeFournisseur;

public record FournisseurRequest(
    @NotBlank String raisonSociale,
    @NotNull TypeFournisseur type,
    String contact,
    String adresseFacturation) {}
