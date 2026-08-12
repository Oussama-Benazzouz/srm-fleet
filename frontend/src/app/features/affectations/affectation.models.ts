export type StatutAffectation = 'ACTIVE' | 'CLOTUREE';

export interface Affectation {
  id: number;
  vehiculeId: number;
  immatriculation: string;
  conducteurId: number;
  conducteurNomComplet: string;
  matriculeConducteur: string;
  provinceId: number | null;
  provinceLibelle: string | null;
  dateDebut: string;
  dateFin: string | null;
  statut: StatutAffectation;
  motif: string | null;
}

export interface AffectationRequest {
  vehiculeId: number;
  conducteurId: number;
  dateDebut: string | null;
  motif: string | null;
}

// provinceId est nécessaire pour respecter la règle « même province » (filtrage du formulaire).
export interface OptionVehicule { id: number; immatriculation: string; provinceId: number | null; }
export interface OptionConducteur { id: number; libelle: string; provinceId: number | null; }