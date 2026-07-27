export type Role = 'ADMINISTRATEUR_CENTRAL' | 'RESPONSABLE_PROVINCIAL';

/** Libellés lisibles des rôles, partagés par la coquille et les écrans d'admin. */
export const ROLE_LABELS: Record<Role, string> = {
  ADMINISTRATEUR_CENTRAL: 'Administrateur central',
  RESPONSABLE_PROVINCIAL: 'Responsable provincial',
};

export interface LoginRequest {
  login: string;
  motDePasse: string;
}

export interface LoginResponse {
  token: string;
  login: string;
  role: Role;
  provinceId: number | null;
}

export interface CurrentUser {
  login: string;
  role: Role;
  provinceId: number | null;
}
