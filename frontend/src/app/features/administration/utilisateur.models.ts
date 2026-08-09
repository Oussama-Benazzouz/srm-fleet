import { Role } from '../../core/models/auth.models';

export type StatutCompte = 'ACTIF' | 'DESACTIVE';

export interface Utilisateur {
  id: number;
  nom: string;
  prenom: string;
  login: string;
  email: string;
  role: Role;
  statutCompte: StatutCompte;
  provinceId: number | null;
}

export interface UtilisateurRequest {
  nom: string;
  prenom: string;
  login: string;
  email: string;
  motDePasse?: string;
  role: Role;
  provinceId: number | null;
}

export interface Province {
  id: number;
  libelle: string;
}

export interface Departement {
  id: number;
  libelle: string;
}
