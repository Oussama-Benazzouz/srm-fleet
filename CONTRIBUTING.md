# Guide de contribution — SRM Fleet

## Branches

- `main` : code livrable, protégée.
- `develop` : intégration continue, protégée.
- `feature/<module>-<description-courte>` : une fonctionnalité.
- `fix/<module>-<description-courte>` : une correction.
- `hotfix/<description-courte>` : correction urgente sur `main`.
- `chore/<description-courte>` : tâche technique.

## Commits — Conventional Commits

Format : `type(scope): description à l'impératif`

Types : `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`, `ci`, `perf`.
Le `scope` est le module concerné (ex. `affectation`, `carte`, `conformite`).

## Pull Requests

1. Rebaser sur `develop` à jour avant d'ouvrir la PR.
2. Une PR = une tâche, petite et ciblée.
3. Remplir le template (objectif, périmètre, comment tester).
4. CI verte obligatoire.
5. Un relecteur (l'autre développeur) obligatoire, pas d'auto-fusion.
6. Fusion en squash vers `develop`.
7. Supprimer la branche après fusion.

## Code

- Backend : injection par constructeur, `@Transactional` en couche service,
  aucune logique métier dans les contrôleurs.
- Frontend : composants standalone, typage strict, pas de `any`.
- Formatage vérifié en CI (Spotless/Google Java Format côté backend,
  ESLint/Prettier côté frontend).