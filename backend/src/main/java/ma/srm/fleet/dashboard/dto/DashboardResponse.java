package ma.srm.fleet.dashboard.dto;

import java.util.List;

/**
 * Instantané agrégé de tous les modules pour le tableau de bord d'accueil (RF transverse).
 * Les compteurs et répartitions sont déjà cloisonnés par province pour un Responsable Provincial.
 */
public record DashboardResponse(
    // --- Indicateurs clés ---
    long vehicules,
    long vehiculesDisponibles,
    long vehiculesAffectes,
    long vehiculesImmobilises,
    long conducteurs,
    long affectationsActives,
    long cartesActives,
    long demandesEnAttente,
    long alertesActives,
    long sinistresOuverts,
    // --- Répartitions pour les graphes ---
    List<CompteParLibelle> vehiculesParStatut,
    List<CompteParLibelle> vehiculesParAcquisition,
    List<CompteParLibelle> vehiculesParProvince,
    List<CompteParLibelle> sinistresParStatut,
    List<CompteParLibelle> alertesParType) {}
