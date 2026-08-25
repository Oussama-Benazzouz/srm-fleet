# Guide de déploiement — serveur interne SRM

Ce guide décrit le déploiement du backoffice **SRM Fleet** sur un serveur interne de
l'entreprise, **accessible uniquement depuis le réseau interne** (aucune exposition sur
Internet).

## 1. Principes

- Toute la plateforme tourne en conteneurs (Docker Compose) : base, backend, frontend.
- **Seul le frontend** est exposé aux utilisateurs, et **uniquement sur le réseau interne**.
- Le **backend** et la **base de données** ne sont **pas** publiés sur l'hôte : ils ne sont
  joignables que par le réseau Docker interne (le frontend Nginx relaie `/api/` vers
  `backend:8080`).
- Aucun secret n'est stocké dans le dépôt : tout passe par le fichier `.env` du serveur.

## 2. Prérequis serveur

- Linux avec **Docker Engine** et **Docker Compose v2**.
- Le serveur est raccordé au réseau interne (adresse LAN, ex. `10.0.0.10`) et **n'a aucune
  redirection de port depuis le pare-feu périmétrique** / pas d'IP publique routable.
- ~2 vCPU / 2–4 Go RAM pour une charge modérée ; espace disque pour la base et les pièces jointes.

## 3. Récupération du code

```bash
git clone <URL_DU_DEPOT_INTERNE> srm-fleet
cd srm-fleet
git checkout main      # ou le tag de version à déployer
```

## 4. Configuration des secrets (`.env`)

```bash
cp .env.example .env
```

Renseigner des valeurs **fortes et propres à la production** :

| Variable | Recommandation |
|----------|----------------|
| `POSTGRES_PASSWORD` | mot de passe aléatoire long |
| `JWT_SECRET` | ≥ 32 octets aléatoires — ex. `openssl rand -base64 48` |
| `APP_ADMIN_INITIAL_PASSWORD` | mot de passe fort pour le compte `admin` (à changer après 1re connexion) |
| `JWT_EXPIRATION_MS` | ex. `86400000` (24 h) |

> `.env` ne doit **jamais** être committé (il est déjà dans `.gitignore`). Restreindre ses
> droits : `chmod 600 .env`.

## 5. Restreindre l'accès au réseau interne

Le `docker-compose.yml` par défaut publie les ports sur toutes les interfaces (`0.0.0.0`), ce
qui convient au développement mais **pas** à la production. Ajouter un fichier de surcharge de
production qui :

- expose le frontend **uniquement sur l'IP interne** du serveur ;
- **ne publie pas** le backend ni la base sur l'hôte.

Créer `docker-compose.prod.yml` :

```yaml
services:
  db:
    ports: !reset []          # la base reste interne au réseau Docker
  backend:
    ports: !reset []          # le backend n'est joignable que via le frontend (réseau Docker)
  frontend:
    ports: !reset
      - "10.0.0.10:80:80"     # remplacer par l'IP interne du serveur
```

Lancer en combinant les deux fichiers (voir §6).

**Défense en profondeur** (recommandé en complément) :

- Pare-feu hôte : n'autoriser le port 80 que depuis le sous-réseau interne. Exemple `ufw` :
  ```bash
  ufw default deny incoming
  ufw allow from 10.0.0.0/8 to any port 80 proto tcp
  ufw enable
  ```
- Optionnel : placer le service derrière le **reverse proxy interne** de l'entreprise avec
  **TLS** (certificat de l'autorité interne), n'acceptant que le trafic du LAN.

## 6. Lancement

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

Vérifications :

```bash
docker compose ps                              # tous les services "healthy"
curl -f http://localhost:80/                   # le frontend répond (depuis le serveur)
docker compose logs -f backend                 # migrations Flyway appliquées, pas d'erreur
```

Depuis un poste **du réseau interne** : ouvrir `http://10.0.0.10/`.

## 7. Première connexion

- Se connecter avec le compte `admin` et le mot de passe `APP_ADMIN_INITIAL_PASSWORD`.
- **Changer immédiatement** ce mot de passe, puis créer les comptes des responsables provinciaux.
- Si la variable n'a pas été fournie, le compte `admin` reste inutilisable (par sécurité) :
  renseigner `APP_ADMIN_INITIAL_PASSWORD` puis `docker compose ... up -d` à nouveau.

## 8. Persistance et sauvegardes

Volumes Docker persistants :

- `db-data` — données PostgreSQL ;
- `documents-data` — pièces jointes des sinistres (`/app/data/documents`).

Sauvegardes recommandées (quotidiennes, hors du serveur) :

```bash
# Base de données
docker compose exec -T db pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB" > backup-$(date +%F).sql

# Pièces jointes
docker run --rm -v srm-fleet_documents-data:/data -v "$PWD":/backup alpine \
  tar czf /backup/documents-$(date +%F).tar.gz -C /data .
```

Restauration : recharger le dump SQL et décompresser l'archive dans le volume correspondant.

## 9. Mises à jour

```bash
git pull
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

Les nouvelles migrations Flyway s'appliquent automatiquement au démarrage du backend.
Effectuer une sauvegarde (§8) avant toute mise à jour.

## 10. Supervision

- Journaux : `docker compose logs -f [service]`.
- Santé : `docker compose ps` (healthchecks sur `db` et `backend`).
- Le moteur d'alertes s'exécute quotidiennement (cron `APP_ALERTES_CRON`, défaut 06:00).

## 11. Checklist de sécurité (handoff)

- [ ] `.env` renseigné avec des secrets forts, `chmod 600`, non committé.
- [ ] `JWT_SECRET` ≥ 32 octets, aléatoire.
- [ ] `APP_ADMIN_INITIAL_PASSWORD` fort ; mot de passe `admin` changé après la 1re connexion.
- [ ] Surcharge de prod appliquée : frontend lié à l'IP interne, backend et base **non publiés**.
- [ ] Pare-feu hôte limité au sous-réseau interne ; aucune redirection depuis le pare-feu périmétrique.
- [ ] Sauvegardes base + pièces jointes planifiées et testées (restauration).
- [ ] Accès `git` au dépôt interne restreint à l'équipe.
