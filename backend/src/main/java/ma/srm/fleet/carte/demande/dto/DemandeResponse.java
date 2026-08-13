package ma.srm.fleet.carte.demande.dto;

import java.time.LocalDate;
import ma.srm.fleet.carte.demande.StatutDemandeCarte;

public record DemandeResponse(
    Long id, Long affectationId, Long vehiculeId, String immatriculation,
    Long provinceId, String provinceLibelle,
    LocalDate dateDemande, StatutDemandeCarte statut, String motif,
    String demandeurLogin, String demandeurNomComplet,
    LocalDate dateTraitement, String validateurLogin) {}
