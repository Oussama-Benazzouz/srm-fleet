-- V4 : affectations, demandes et cartes carburant
-- Porte les deux contraintes critiques du projet : RG3 et RG4 (index uniques partiels)

CREATE TABLE affectation (
     id_affectation         BIGSERIAL PRIMARY KEY,
     date_debut_affectation DATE NOT NULL,
     date_fin_affectation   DATE,
     motif_affectation      VARCHAR(255),
     statut_affectation     VARCHAR(20) NOT NULL,
     id_vehicule   BIGINT NOT NULL REFERENCES vehicule(id_vehicule),
     id_conducteur BIGINT NOT NULL REFERENCES conducteur(id_conducteur),
     created_at TIMESTAMP NOT NULL DEFAULT now(),
     updated_at TIMESTAMP,
     created_by VARCHAR(100),
     updated_by VARCHAR(100),
     CONSTRAINT chk_dates_affectation
     CHECK (date_fin_affectation IS NULL OR date_fin_affectation >= date_debut_affectation)
);

-- RG3 / RF-11 : une seule affectation ACTIVE par vehicule, garantie par la base
CREATE UNIQUE INDEX uq_affectation_active_par_vehicule
  ON affectation (id_vehicule)
  WHERE statut_affectation = 'ACTIVE';

CREATE INDEX idx_affectation_conducteur ON affectation (id_conducteur);

CREATE TABLE demande_carte_carburant (
     id_demande                    BIGSERIAL PRIMARY KEY,
     date_demande_carte            DATE NOT NULL,
     motif_demande_carte           VARCHAR(255),
     statut_demande_carte          VARCHAR(20) NOT NULL,
     date_traitement_demande_carte DATE,
     id_affectation            BIGINT NOT NULL REFERENCES affectation(id_affectation),
     id_utilisateur_demandeur  BIGINT NOT NULL REFERENCES utilisateur(id_utilisateur),
     id_utilisateur_validateur BIGINT REFERENCES utilisateur(id_utilisateur),
     created_at TIMESTAMP NOT NULL DEFAULT now(),
     updated_at TIMESTAMP,
     created_by VARCHAR(100),
     updated_by VARCHAR(100)
);
CREATE INDEX idx_demande_affectation ON demande_carte_carburant (id_affectation);

CREATE TABLE carte_carburant (
     id_carte              BIGSERIAL PRIMARY KEY,
     numero_carte          VARCHAR(50) NOT NULL UNIQUE,
     plafond_montant_carte NUMERIC(10,2) NOT NULL CHECK (plafond_montant_carte > 0),
     statut_carte          VARCHAR(20) NOT NULL,
     date_creation_carte   DATE NOT NULL,
     id_affectation        BIGINT NOT NULL REFERENCES affectation(id_affectation),
     id_fournisseur        BIGINT NOT NULL REFERENCES fournisseur(id_fournisseur),
     id_demande            BIGINT NOT NULL REFERENCES demande_carte_carburant(id_demande),
     created_at            TIMESTAMP NOT NULL DEFAULT now(),
     updated_at            TIMESTAMP,
     created_by VARCHAR(100),
     updated_by VARCHAR(100)
);

-- RG4 / RF-14 : une seule carte ACTIVE par affectation, garantie par la base
CREATE UNIQUE INDEX uq_carte_active_par_affectation
  ON carte_carburant (id_affectation)
  WHERE statut_carte = 'ACTIVE';

CREATE INDEX idx_carte_affectation ON carte_carburant (id_affectation);
