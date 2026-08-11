export type ModeAcquisition = 'PROPRIETE' | 'LOCATION';
export type StatutVehicule = 'DISPONIBLE' | 'AFFECTE' | 'IMMOBILISE' | 'SORTI_DU_PARC';
export type TypeFournisseur = 'LOUEUR' | 'CARBURANT' | 'ASSURANCE' | 'GARAGE';

export interface Fournisseur {
  id: number;
  raisonSociale: string;
  type: TypeFournisseur;
  contact: string | null;
  adresseFacturation: string | null;
}

export interface FournisseurRequest {
  raisonSociale: string;
  type: TypeFournisseur;
  contact: string | null;
  adresseFacturation: string | null;
}

export interface Vehicule {
  id: number;
  immatriculation: string;
  typeVehicule: string;
  modeAcquisition: ModeAcquisition;
  dateMiseEnService: string | null;
  statut: StatutVehicule;
  numeroContrat: string | null;
  dateDebutContrat: string | null;
  dateFinContrat: string | null;
  loyerMensuel: number | null;
  plafondKm: number | null;
  provinceId: number | null;
  provinceLibelle: string | null;
  departementId: number | null;
  departementLibelle: string | null;
  fournisseurId: number | null;
  fournisseurRaisonSociale: string | null;
}

export interface VehiculeRequest {
  immatriculation: string;
  typeVehicule: string;
  modeAcquisition: ModeAcquisition;
  dateMiseEnService: string | null;
  statut: StatutVehicule;
  numeroContrat: string | null;
  dateDebutContrat: string | null;
  dateFinContrat: string | null;
  loyerMensuel: number | null;
  plafondKm: number | null;
  provinceId: number | null;
  departementId: number | null;
  fournisseurId: number | null;
}

export interface RechercheVehicule {
  immatriculation?: string;
  type?: string;
  statut?: StatutVehicule | '';
}

export const STATUT_LABELS: Record<StatutVehicule, string> = {
  DISPONIBLE: 'Disponible',
  AFFECTE: 'Affecté',
  IMMOBILISE: 'Immobilisé',
  SORTI_DU_PARC: 'Sorti du parc',
};

export const MODE_LABELS: Record<ModeAcquisition, string> = {
  PROPRIETE: 'Propriété',
  LOCATION: 'Location',
};

export const TYPE_FOURNISSEUR_LABELS: Record<TypeFournisseur, string> = {
  LOUEUR: 'Loueur',
  CARBURANT: 'Carburant',
  ASSURANCE: 'Assurance',
  GARAGE: 'Garage',
};
