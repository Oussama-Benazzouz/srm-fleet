export type TypeSourceAlerte = 'ASSURANCE' | 'VISITE_TECHNIQUE';
export type StatutLecture = 'NON_LUE' | 'LUE';

/** Alerte du tableau de bord (RF-22). */
export interface Alerte {
  id: number;
  typeSource: TypeSourceAlerte;
  vehiculeId: number;
  immatriculation: string;
  provinceId: number | null;
  provinceLibelle: string | null;
  dateEcheance: string;
  joursRestants: number;
  conducteurNomComplet: string | null;
  statutLecture: StatutLecture;
  dateDeclenchement: string;
}

/** Seuil paramétrable par type d'échéance surveillé (RF-20). */
export interface ConfigurationAlerte {
  typeSource: TypeSourceAlerte;
  seuilJours: number;
  canalDefaut: string;
}

export interface GenerationResult {
  creees: number;
}

export const TYPE_SOURCE_LABELS: Record<TypeSourceAlerte, string> = {
  ASSURANCE: 'Assurance',
  VISITE_TECHNIQUE: 'Visite technique',
};
