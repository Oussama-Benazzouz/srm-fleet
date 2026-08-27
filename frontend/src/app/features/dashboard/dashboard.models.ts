export interface CompteParLibelle {
  libelle: string;
  valeur: number;
}

/** Instantané agrégé renvoyé par GET /api/v1/tableau-de-bord. */
export interface DashboardData {
  vehicules: number;
  vehiculesDisponibles: number;
  vehiculesAffectes: number;
  vehiculesImmobilises: number;
  conducteurs: number;
  affectationsActives: number;
  cartesActives: number;
  demandesEnAttente: number;
  alertesActives: number;
  sinistresOuverts: number;
  vehiculesParStatut: CompteParLibelle[];
  vehiculesParAcquisition: CompteParLibelle[];
  vehiculesParProvince: CompteParLibelle[];
  sinistresParStatut: CompteParLibelle[];
  alertesParType: CompteParLibelle[];
}

/** Part d'un anneau : un libellé, une valeur et sa couleur. */
export interface Segment {
  libelle: string;
  valeur: number;
  couleur: string;
}
