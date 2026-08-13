package ma.srm.fleet.carte.cartecarburant.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import ma.srm.fleet.carte.cartecarburant.StatutCarte;

public record CarteResponse(
    Long id, String numeroCarte, BigDecimal plafondMontant, StatutCarte statut,
    Long affectationId, Long vehiculeId, String immatriculation,
    Long provinceId, String provinceLibelle,
    Long fournisseurId, String fournisseurRaisonSociale,
    Long demandeId, LocalDate dateCreation) {}
