# ADR 0003 — Alertes in-app uniquement + retrait de l'alerte plafond de carte

- Date : 2026-08-25
- Statut : Accepté

## Contexte

Le cahier des charges prévoyait des alertes sur deux canaux (tableau de bord + courriel) et une
alerte d'approche du plafond de carte carburant (RF-16, RF-21).

Deux contraintes sont apparues :
- pas de service SMTP disponible en production (API payante non validée) ;
- aucun suivi de consommation des cartes dans l'application (le rapprochement est fait en fin de
  mois par la comptabilité, via le portail du fournisseur de carburant).

## Décision

- **Notifications in-app uniquement** (canal tableau de bord) ; le canal courriel est reporté.
  La colonne `canal_defaut` reste en base pour une évolution future.
- **Retrait du type d'alerte `PLAFOND_CARTE`** (enum + configuration, migration V8). Le moteur
  ne surveille que les échéances d'assurance et de visite technique.

## Conséquences

- Le moteur d'alertes et le tableau de bord fonctionnent sans dépendance externe.
- Depuis le tableau de bord, les gestionnaires contactent manuellement le conducteur/responsable.
- Le rapprochement des plafonds reste un processus manuel externe, documenté comme tel.
- Réintroduire le courriel ou le suivi de consommation nécessitera une nouvelle décision.
