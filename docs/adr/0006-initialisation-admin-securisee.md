# ADR 0006 — Initialisation sécurisée du compte administrateur

- Date : 2026-08-25
- Statut : Accepté

## Contexte

Le seed SQL ne peut pas produire de hash BCrypt ; il insère un placeholder, remplacé au
démarrage par `AdminPasswordInitializer`. La version initiale codait un mot de passe par défaut
en dur (`Admin@123`) appliqué **quel que soit le profil**, et le journalisait en clair — soit un
compte administrateur au mot de passe connu sur un déploiement de production.

## Décision

- Le mot de passe initial du compte `admin` provient **exclusivement de la configuration**
  (`app.admin.initial-password` / `APP_ADMIN_INITIAL_PASSWORD`).
- **Aucun mot de passe par défaut** n'est appliqué : si la variable est absente, le compte reste
  inutilisable et un message d'erreur invite à la définir.
- Le mot de passe n'est plus journalisé. En développement, `application-dev.yml` fournit une
  valeur de confort.

## Conséquences

- Un déploiement de production ne crée jamais de compte administrateur au mot de passe connu.
- Le déploiement exige de définir `APP_ADMIN_INITIAL_PASSWORD` (documenté dans le guide de
  déploiement) ; le mot de passe est à changer après la première connexion.
