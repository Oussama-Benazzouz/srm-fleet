# ADR 0005 — Tests d'intégration via Testcontainers

- Date : 2026-08-25
- Statut : Accepté

## Contexte

Les contraintes critiques du projet (RG3 : une affectation active par véhicule ; RG4 : une carte
active par affectation) reposent sur des **index uniques partiels PostgreSQL**, non reproductibles
avec une base en mémoire. La dépendance H2 présente dans le `pom` n'était pas représentative et
n'était plus utilisée.

## Décision

- Introduire des **tests d'intégration Testcontainers** (PostgreSQL réel) via une base
  `AbstractPostgresIntegrationTest` (conteneur unique, `@DynamicPropertySource`).
- Valider les migrations Flyway, le seed, et le rejet en base d'une seconde ressource active
  (RG3/RG4).
- **Retirer la dépendance H2**.

## Conséquences

- Les garanties spécifiques à PostgreSQL sont testées automatiquement (Docker requis, déjà
  nécessaire au projet et présent en CI).
- Les nouveaux tests d'intégration étendent la base commune et réutilisent le conteneur.
