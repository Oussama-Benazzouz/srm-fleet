CREATE TABLE fournisseur (
                           id_fournisseur                  BIGSERIAL PRIMARY KEY,
                           raison_sociale_fournisseur      VARCHAR(150) NOT NULL,
                           type_fournisseur                VARCHAR(30)  NOT NULL,
                           contact_fournisseur             VARCHAR(150),
                           adresse_facturation_fournisseur VARCHAR(255),
                           created_at TIMESTAMP NOT NULL DEFAULT now(),
                           updated_at TIMESTAMP,
                           created_by VARCHAR(100),
                           updated_by VARCHAR(100)
);

CREATE TABLE vehicule (
                        id_vehicule                 BIGSERIAL PRIMARY KEY,
                        immatriculation_vehicule    VARCHAR(20)  NOT NULL UNIQUE,
                        type_vehicule               VARCHAR(50)  NOT NULL,
                        mode_acquisition_vehicule   VARCHAR(20)  NOT NULL,
                        date_mise_service_vehicule  DATE,
                        statut_vehicule             VARCHAR(20)  NOT NULL,
                        numero_contrat_vehicule     VARCHAR(50),
                        date_debut_contrat_vehicule DATE,
                        date_fin_contrat_vehicule   DATE,
                        loyer_mensuel_vehicule      NUMERIC(10,2),
                        plafond_km_vehicule         INTEGER,
                        id_province    BIGINT NOT NULL REFERENCES province(id_province),
                        id_departement BIGINT REFERENCES departement(id_departement),
                        id_fournisseur BIGINT REFERENCES fournisseur(id_fournisseur),
                        created_at TIMESTAMP NOT NULL DEFAULT now(),
                        updated_at TIMESTAMP,
                        created_by VARCHAR(100),
                        updated_by VARCHAR(100)
);

COMMENT ON COLUMN vehicule.id_province    IS 'RG1 : obligatoire';
COMMENT ON COLUMN vehicule.id_fournisseur IS 'RG2 : obligatoire si location (regle applicative Phase 5)';
CREATE INDEX idx_vehicule_province ON vehicule (id_province);
CREATE INDEX idx_vehicule_statut   ON vehicule (statut_vehicule);
