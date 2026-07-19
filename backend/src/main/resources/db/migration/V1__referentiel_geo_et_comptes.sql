-- V1 : referentiel geographique, organisationnel et comptes utilisateurs
-- Tables : province, departement, utilisateur

CREATE TABLE province (
    id_province       BIGSERIAL PRIMARY KEY,
    libelle_province  VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE departement (
    id_departement       BIGSERIAL PRIMARY KEY,
    libelle_departement  VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE utilisateur (
    id_utilisateur            BIGSERIAL PRIMARY KEY,
    nom_utilisateur           VARCHAR(100) NOT NULL,
    prenom_utilisateur        VARCHAR(100) NOT NULL,
    login_utilisateur         VARCHAR(100) NOT NULL UNIQUE,
    email_utilisateur         VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe_utilisateur  VARCHAR(255) NOT NULL,
    role_utilisateur          VARCHAR(30)  NOT NULL,
    statut_compte_utilisateur VARCHAR(20)  NOT NULL,
    id_province               BIGINT REFERENCES province(id_province),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

COMMENT ON COLUMN utilisateur.id_province IS 'NULL si administrateur central (RG14)';