# ADR 0002 — Mappers manuels plutôt que MapStruct

- Date : 2026-08-25
- Statut : Accepté

## Contexte

Le plan évoquait MapStruct pour la conversion entité ↔ DTO. Le code utilise des **mappers
statiques manuels** (ex. `AffectationMapper`) ou des méthodes `toResponse(...)` privées dans les
services.

## Décision

Conserver des mappers manuels, sans MapStruct.

## Conséquences

- Aucune génération de code ni dépendance/traitement d'annotation supplémentaire.
- Le mapping reste explicite et lisible ; convention à respecter pour les nouveaux modules.
- Règle maintenue : les entités JPA ne quittent jamais la couche service ; le contrôleur ne
  parle qu'en DTO.
