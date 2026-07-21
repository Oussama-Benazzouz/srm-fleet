export type Role = 'ADMINISTRATEUR_CENTRAL' | 'RESPONSABLE_PROVINCIAL';

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
