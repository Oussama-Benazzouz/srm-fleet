package ma.srm.fleet.referentiel.vehicule.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import ma.srm.fleet.referentiel.vehicule.ModeAcquisition;
import ma.srm.fleet.referentiel.vehicule.StatutVehicule;

public record VehiculeResponse(
    Long id, String immatriculation, String typeVehicule, ModeAcquisition modeAcquisition,
    LocalDate dateMiseEnService, StatutVehicule statut, String numeroContrat,
    LocalDate dateDebutContrat, LocalDate dateFinContrat, BigDecimal loyerMensuel, Integer plafondKm,
    Long provinceId, String provinceLibelle, Long departementId, String departementLibelle,
    Long fournisseurId, String fournisseurRaisonSociale) {}
