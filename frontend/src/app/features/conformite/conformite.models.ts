/** Échéance d'assurance d'un véhicule (RF-18). */
export interface Assurance {
  id: number;
  vehiculeId: number;
  immatriculation: string;
  provinceId: number | null;
  provinceLibelle: string | null;
  nomCompagnie: string;
  dateDebut: string;
  dateEcheance: string;
}

export interface AssuranceRequest {
  vehiculeId: number | null;
  nomCompagnie: string | null;
  dateDebut: string | null;
  dateEcheance: string | null;
}

/** Visite technique (RF-19). Le conducteur accompagnateur est dérivé côté backend (RG11). */
export interface Visite {
  id: number;
  vehiculeId: number;
  immatriculation: string;
  provinceId: number | null;
  provinceLibelle: string | null;
  dateVisite: string;
  dateEcheance: string;
  nomCompagnie: string;
  conducteurId: number;
  conducteurNomComplet: string;
  matriculeConducteur: string;
}

export interface VisiteRequest {
  vehiculeId: number | null;
  dateVisite: string | null;
  dateEcheance: string | null;
  nomCompagnie: string | null;
}

/** Option de sélection d'un véhicule (déjà scopé par le backend selon le rôle). */
export interface OptionVehicule {
  id: number;
  libelle: string;
}

/** Cycle de vie d'un sinistre (RF-24). */
export type StatutSinistre = 'OUVERT' | 'EN_COURS' | 'CLOTURE';

/** Sinistre déclaré sur un véhicule (RF-23). */
export interface Sinistre {
  id: number;
  vehiculeId: number;
  immatriculation: string;
  provinceId: number | null;
  provinceLibelle: string | null;
  dateSinistre: string;
  description: string | null;
  statut: StatutSinistre;
  affectationId: number | null;
  conducteurId: number | null;
  conducteurNomComplet: string | null;
}

export interface SinistreRequest {
  vehiculeId: number | null;
  dateSinistre: string | null;
  description: string | null;
}

/** Pièce jointe rattachée à un sinistre (RF-24). */
export interface PieceJointe {
  id: number;
  sinistreId: number;
  nomFichier: string;
  typeDocument: string | null;
  dateUpload: string;
}

export const STATUT_SINISTRE_LABELS: Record<StatutSinistre, string> = {
  OUVERT: 'Ouvert',
  EN_COURS: 'En cours',
  CLOTURE: 'Clôturé',
};

/** Statut suivant dans l'ordre ouvert → en_cours → clôturé (null si déjà clôturé). */
export const STATUT_SINISTRE_SUIVANT: Record<StatutSinistre, StatutSinistre | null> = {
  OUVERT: 'EN_COURS',
  EN_COURS: 'CLOTURE',
  CLOTURE: null,
};
