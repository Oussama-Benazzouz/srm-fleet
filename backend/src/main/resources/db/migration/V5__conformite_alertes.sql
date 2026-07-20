-- V5 : conformite (assurance, visite technique, sinistre, document) et alertes

CREATE TABLE assurance (
    id_assurance            BIGSERIAL PRIMARY KEY,
    nom_compagnie_assurance VARCHAR(150) NOT NULL,
    date_debut_assurance    DATE NOT NULL,
    date_echeance_assurance DATE NOT NULL,
    id_vehicule             BIGINT NOT NULL REFERENCES vehicule(id_vehicule),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);
CREATE INDEX idx_assurance_vehicule ON assurance (id_vehicule);

CREATE TABLE visite_technique (
    id_visite            BIGSERIAL PRIMARY KEY,
    date_visite          DATE NOT NULL,
    date_echeance_visite DATE NOT NULL,
    nom_compagnie_visite VARCHAR(150) NOT NULL,
    id_vehicule          BIGINT NOT NULL REFERENCES vehicule(id_vehicule),
    id_conducteur        BIGINT NOT NULL REFERENCES conducteur(id_conducteur),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);
COMMENT ON COLUMN visite_technique.id_conducteur
    IS 'Conducteur accompagnateur = titulaire de l''affectation active a la date (RG11, controle applicatif Phase 8)';
CREATE INDEX idx_visite_vehicule ON visite_technique (id_vehicule);

CREATE TABLE sinistre (
    id_sinistre          BIGSERIAL PRIMARY KEY,
    date_sinistre        DATE NOT NULL,
    description_sinistre TEXT,
    statut_sinistre      VARCHAR(20) NOT NULL,
    id_vehicule          BIGINT NOT NULL REFERENCES vehicule(id_vehicule),
    id_affectation       BIGINT REFERENCES affectation(id_affectation),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);
COMMENT ON COLUMN sinistre.id_affectation IS 'NULL si aucune affectation active a la date des faits (RG7)';
CREATE INDEX idx_sinistre_vehicule ON sinistre (id_vehicule);

CREATE TABLE document (
    id_document              BIGSERIAL PRIMARY KEY,
    nom_fichier_document     VARCHAR(255) NOT NULL,
    type_document            VARCHAR(50),
    chemin_stockage_document VARCHAR(500) NOT NULL,
    date_upload_document     TIMESTAMP NOT NULL DEFAULT now(),
    id_sinistre              BIGINT NOT NULL REFERENCES sinistre(id_sinistre),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);
CREATE INDEX idx_document_sinistre ON document (id_sinistre);

CREATE TABLE configuration_alerte (
    type_source_alerte  VARCHAR(30) PRIMARY KEY,
    seuil_jours_alerte  INTEGER NOT NULL CHECK (seuil_jours_alerte > 0),
    canal_defaut_alerte VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE alerte (
    id_alerte                 BIGSERIAL PRIMARY KEY,
    type_source_alerte        VARCHAR(30) NOT NULL REFERENCES configuration_alerte(type_source_alerte),
    id_reference_alerte       BIGINT NOT NULL,
    date_declenchement_alerte TIMESTAMP NOT NULL DEFAULT now(),
    canal_alerte              VARCHAR(20) NOT NULL,
    statut_lecture_alerte     VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);
COMMENT ON COLUMN alerte.id_reference_alerte IS 'Id de l''objet source (assurance, visite ou carte) selon type_source_alerte';
CREATE INDEX idx_alerte_source ON alerte (type_source_alerte, id_reference_alerte);