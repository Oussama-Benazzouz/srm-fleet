-- V3 : conducteurs et permis (RG9 : plusieurs permis par conducteur)

CREATE TABLE conducteur (
    id_conducteur             BIGSERIAL PRIMARY KEY,
    matricule_conducteur      VARCHAR(50) NOT NULL UNIQUE,
    nom_conducteur            VARCHAR(100) NOT NULL,
    prenom_conducteur         VARCHAR(100) NOT NULL,
    fonction_conducteur       VARCHAR(100),
    groupe_sanguin_conducteur VARCHAR(3),
    id_departement            BIGINT REFERENCES departement(id_departement),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE permis (
    id_permis                BIGSERIAL PRIMARY KEY,
    type_permis              VARCHAR(10) NOT NULL,
    date_fin_validite_permis DATE NOT NULL,
    id_conducteur            BIGINT NOT NULL REFERENCES conducteur(id_conducteur),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_permis_conducteur ON permis (id_conducteur);