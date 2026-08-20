package ma.srm.fleet.dashboard.dto;

/** Une part d'un graphe : un libellé et sa valeur (utilisé pour anneaux et barres). */
public record CompteParLibelle(String libelle, long valeur) {}
