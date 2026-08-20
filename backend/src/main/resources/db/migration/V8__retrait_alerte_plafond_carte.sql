-- V8 : retrait du type d'alerte PLAFOND_CARTE
-- Le rapprochement du plafond de carte est effectue manuellement en fin de mois par la
-- comptabilite via le portail du fournisseur de carburant. Aucune donnee de consommation
-- n'est suivie dans l'application, donc aucune alerte de plafond ne peut etre generee :
-- on retire le type de la configuration (l'enum applicatif ne le connait plus non plus).

DELETE FROM configuration_alerte WHERE type_source_alerte = 'PLAFOND_CARTE';
