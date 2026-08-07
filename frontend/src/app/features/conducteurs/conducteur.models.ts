export interface Permis {
  id: number;
  typePermis: string;
  dateFinValidite: string;
}

export interface Conducteur {
  id: number;
  matricule: string;
  nom: string;
  prenom: string;
  fonction: string | null;
  groupeSanguin: string | null;
  provinceId: number;
  provinceLibelle: string;
  departementId: number;
  departementLibelle: string;
  permis: Permis[];
}

export interface ConducteurRequest {
  matricule: string;
  nom: string;
  prenom: string;
  fonction: string | null;
  groupeSanguin: string | null;
  provinceId: number | null;
  departementId: number | null;
}

export interface ConducteurFilter {
  recherche?: string;
  provinceId?: number | null;
  departementId?: number | null;
}

export interface PermisRequest {
  typePermis: string;
  dateFinValidite: string;
}