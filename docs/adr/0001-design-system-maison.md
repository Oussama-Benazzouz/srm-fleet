# ADR 0001 — Design system maison plutôt qu'Angular Material

- Date : 2026-08-25
- Statut : Accepté

## Contexte

Le cahier des charges et le plan initial mentionnaient Angular Material. En pratique, le
frontend a été construit sur un **design system maison** (tokens et primitives CSS dans
`frontend/src/styles.scss` : `.card`, `.btn`, `.field`, `.table`, `.badge`, `.select`…).

## Décision

Ne pas intégrer Angular Material ; composer chaque écran à partir des primitives du design
system maison. Aucune librairie UI externe.

## Conséquences

- Cohérence visuelle totale et surface de dépendances réduite.
- Toute nouvelle vue réutilise les primitives existantes plutôt que de redéfinir ses styles.
- Les graphes du tableau de bord sont dessinés en SVG maison (cf. ADR 0004 pour les exports).
