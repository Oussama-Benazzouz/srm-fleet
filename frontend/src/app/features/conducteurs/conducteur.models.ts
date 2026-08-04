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
  departementId: number | null;
  departementLibelle: string | null;
  permis: Permis[];
}

export interface ConducteurRequest {
  matricule: string;
  nom: string;
  prenom: string;
  fonction: string | null;
  groupeSanguin: string | null;
  departementId: number | null;
}

export interface PermisRequest {
  typePermis: string;
  dateFinValidite: string;
}