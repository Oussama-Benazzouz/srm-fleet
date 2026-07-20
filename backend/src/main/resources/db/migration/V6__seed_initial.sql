-- V6 : jeu de donnees initial (provinces Souss-Massa, admin central, seuils d'alerte)

INSERT INTO province (libelle_province) VALUES
    ('Agadir Ida-Outanane'),
    ('Inezgane-Ait Melloul'),
    ('Chtouka-Ait Baha'),
    ('Taroudant'),
    ('Tiznit'),
    ('Tata');

INSERT INTO departement (libelle_departement) VALUES ('DSI'), ('Logistique');

-- Le hash BCrypt reel sera pose en Phase 3 (authentification).
INSERT INTO utilisateur (nom_utilisateur, prenom_utilisateur, login_utilisateur,
    email_utilisateur, mot_de_passe_utilisateur, role_utilisateur,
    statut_compte_utilisateur, id_province)
VALUES ('Admin', 'Central', 'admin', 'admin@srm.ma',
    'A_DEFINIR_PHASE_3', 'ADMINISTRATEUR_CENTRAL', 'ACTIF', NULL);

INSERT INTO configuration_alerte (type_source_alerte, seuil_jours_alerte, canal_defaut_alerte) VALUES
    ('ASSURANCE', 30, 'EMAIL'),
    ('VISITE_TECHNIQUE', 30, 'EMAIL'),
    ('PLAFOND_CARTE', 7, 'EMAIL');