# ADR 0004 — Exports CSV/PDF (OpenPDF) aux couleurs de la charte

- Date : 2026-08-25
- Statut : Accepté

## Contexte

RF-25/RF-26 exigent l'export des données de chaque module au format CSV et/ou PDF, reflétant les
filtres affichés, avec avertissement si aucun filtre n'est appliqué.

## Décision

- **CSV** généré sans dépendance (UTF-8 + BOM pour Excel, séparateur `;`, échappement RFC 4180).
- **PDF** via **OpenPDF**, mis en page selon la charte graphique SRM Souss-Massa : logo centré
  (`classpath:/branding/logo-srm.*`), filet tricolore officiel (`#14508f` / `#5ab24b` / `#fab61d`),
  tableau à en-tête bleu, pied de page (date, mention, numéro de page).
- Les exports **réutilisent les services de liste** : mêmes filtres et même cloisonnement
  province que les écrans. Un bouton « Exporter » (menu CSV/PDF) est présent sur chaque liste ;
  un avertissement s'affiche si aucun filtre n'est actif.

## Conséquences

- Un seul générateur PDF pour tous les modules ; changer le logo = remplacer le fichier ressource.
- Le typage Helvetica d'OpenPDF tient lieu d'équivalent PDF de l'Arial imposé par la charte.
