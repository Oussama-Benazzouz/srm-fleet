package ma.srm.fleet.referentiel.vehicule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import ma.srm.fleet.referentiel.vehicule.ModeAcquisition;
import ma.srm.fleet.referentiel.vehicule.StatutVehicule;

public record VehiculeRequest(
    @NotBlank String immatriculation,
    @NotBlank String typeVehicule,
    @NotNull ModeAcquisition modeAcquisition,
    LocalDate dateMiseEnService,
    @NotNull StatutVehicule statut,
    String numeroContrat,
    LocalDate dateDebutContrat,
    LocalDate dateFinContrat,
    BigDecimal loyerMensuel,
    Integer plafondKm,
    @NotNull Long provinceId,
    Long departementId,
    Long fournisseurId) {}
