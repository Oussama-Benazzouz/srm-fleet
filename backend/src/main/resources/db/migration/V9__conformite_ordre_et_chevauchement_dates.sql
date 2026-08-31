-- V9 : contraintes de conformité au niveau base (filet de sécurité, doublant le contrôle applicatif).
--   1) ordre des dates : la date d'échéance ne peut être antérieure à la date de début (CHECK) ;
--   2) non-chevauchement : deux périodes du même véhicule ne peuvent se chevaucher (EXCLUDE USING gist),
--      ce qui rejette aussi les insertions concurrentes que le contrôle applicatif ne verrait pas.
-- Bornes de période inclusives [début, échéance] : partager une seule journée = chevauchement.

-- Opérateur d'égalité (=) sur les colonnes scalaires (id_vehicule) au sein d'un index GiST.
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE assurance
    ADD CONSTRAINT chk_assurance_ordre_dates
        CHECK (date_echeance_assurance >= date_debut_assurance),
    ADD CONSTRAINT excl_assurance_chevauchement
        EXCLUDE USING gist (
            id_vehicule WITH =,
            daterange(date_debut_assurance, date_echeance_assurance, '[]') WITH &&
        );

ALTER TABLE visite_technique
    ADD CONSTRAINT chk_visite_ordre_dates
        CHECK (date_echeance_visite >= date_visite),
    ADD CONSTRAINT excl_visite_chevauchement
        EXCLUDE USING gist (
            id_vehicule WITH =,
            daterange(date_visite, date_echeance_visite, '[]') WITH &&
        );
