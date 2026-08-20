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
