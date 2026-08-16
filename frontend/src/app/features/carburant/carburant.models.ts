export type StatutCarte = 'ACTIVE' | 'CLOTUREE';
export type StatutDemande = 'EN_ATTENTE' | 'VALIDEE' | 'REFUSEE';

export interface Demande {
  id: number;
  affectationId: number;
  vehiculeId: number;
  immatriculation: string;
  provinceId: number | null;
  provinceLibelle: string | null;
  dateDemande: string;
  statut: StatutDemande;
  motif: string | null;
  demandeurLogin: string | null;
  demandeurNomComplet: string | null;
  dateTraitement: string | null;
  validateurLogin: string | null;
}

export interface DemandeRequest {
  affectationId: number | null;
  motif: string | null;
}
export interface ValiderRequest {
  numeroCarte: string;
  plafondMontant: number | null;
  fournisseurId: number | null;
}

export interface Carte {
  id: number;
  numeroCarte: string;
  plafondMontant: number;
  statut: StatutCarte;
  affectationId: number;
  vehiculeId: number;
  immatriculation: string;
  provinceId: number | null;
  provinceLibelle: string | null;
  fournisseurId: number;
  fournisseurRaisonSociale: string;
  demandeId: number;
  dateCreation: string;
}

export interface OptionAffectation {
  id: number;
  libelle: string;
}
export interface OptionFournisseur {
  id: number;
  raisonSociale: string;
}

export const STATUT_CARTE_LABELS: Record<StatutCarte, string> = {
  ACTIVE: 'Active',
  CLOTUREE: 'Clôturée',
};
export const STATUT_DEMANDE_LABELS: Record<StatutDemande, string> = {
  EN_ATTENTE: 'En attente',
  VALIDEE: 'Validée',
  REFUSEE: 'Refusée',
};
