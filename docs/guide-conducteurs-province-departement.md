# Guide d'exécution — Conducteurs : Province, Département, filtrage et portée provinciale

> **But :** rattacher chaque conducteur à une **Province** et un **Département** (indépendants),
> permettre le **filtrage** par province / département / les deux, et durcir la **sécurité** :
> l'Administrateur Central garde tous les droits, le Responsable Provincial passe en
> **lecture seule limitée à sa propre province**.
>
> **Public :** développeur qui implémente. Suis les étapes **dans l'ordre**, du début jusqu'à la PR.
> **Périmètre principal :** backend (Spring Boot 3.5). Une annexe frontend optionnelle est fournie en fin de document.

---

## 0. Interprétation des règles métier (à valider avant de coder)

| Règle | Interprétation retenue | Conséquence technique |
|------|------------------------|------------------------|
| Conducteur rattaché à une Province **et** un Département | Deux références **obligatoires** (`@NotNull`, `nullable=false`). La table `conducteur` est vide (aucun seed), donc `NOT NULL` est sûr. | Nouveau champ `province` + `id_departement` passe `NOT NULL`. |
| **Aucune** relation Province ↔ Département | Les deux tables restent totalement indépendantes. Le conducteur porte **deux FK séparées** (une vers `province`, une vers `departement`). | On n'ajoute **aucune** FK/relation entre `province` et `departement`. C'est déjà le cas aujourd'hui — on ne touche pas à ces entités. |
| Admin Central : CRUD complet + voit tout | `hasRole('ADMINISTRATEUR_CENTRAL')` sur les écritures ; aucune restriction de portée en lecture. | Ajout d'un endpoint **DELETE conducteur** (manquant aujourd'hui). |
| Responsable Provincial : lecture seule, **sa** province uniquement | Interdit en écriture (403). En lecture, ne voit que les conducteurs `province = saProvince`. Un accès direct à un conducteur d'une autre province → **404** (on ne révèle pas son existence). | Filtrage de portée appliqué **côté service** (pas seulement UI), en plus des `@PreAuthorize`. |
| Filtrage province / département / combinaison, extensible | Filtres optionnels combinables via **JPA Specifications** (chaque critère = un prédicat, `null` = ignoré). | Ajouter un futur critère = ajouter une méthode de `Specification`, sans toucher au reste. |

> **Point de sécurité important déjà présent dans le code** : aujourd'hui le `ConducteurController`
> autorise `RESPONSABLE_PROVINCIAL` en création/modification/permis, et la lecture n'est **pas**
> filtrée par province → un responsable voit et modifie les conducteurs de **toutes** les provinces.
> Cette évolution corrige ce double défaut.

---

## 1. Analyse de l'existant (ce qui est déjà en place)

Fichiers à (re)lire avant de commencer :

- `conducteur/conducteur/Conducteur.java` — entité : a déjà `departement` (LAZY, nullable) et `permis` (`@OneToMany`). **Pas** de province.
- `conducteur/conducteur/ConducteurService.java` — mapping inline (`toResponse`/`toPermis`), `resoudreDepartement`, recherche texte.
- `conducteur/conducteur/ConducteurController.java` — `@PreAuthorize` **trop laxiste** sur les écritures ; pas de DELETE conducteur.
- `conducteur/conducteur/ConducteurRepository.java` — `existsByMatricule` + `@Query rechercher`.
- `admin/utilisateur/Utilisateur.java` — porte déjà `province` (nullable = admin central, RG14).
- `security/UserPrincipal.java` — expose déjà **`getRole()`** et **`getProvinceId()`** → tout ce qu'il faut pour scoper.
- `referentiel/province/Province.java` + `referentiel/departement/Departement.java` — entités **indépendantes** (aucune relation entre elles). ✅ conforme à la règle métier.
- `referentiel/vehicule/Vehicule.java` — **précédent** : porte déjà `province` (obligatoire) **et** `departement`. On s'aligne dessus.
- `security/SecurityExceptionHandler.java` — `AccessDeniedException → 403` déjà géré (RFC 7807).
- `db/migration/V3__conducteurs_permis.sql` (schéma actuel) et `V6__seed_initial.sql` (provinces + départements seedés, **aucun** conducteur).

**Conclusion d'analyse :** l'infrastructure de sécurité (rôle + province dans `UserPrincipal`) et le précédent
géographique (`Vehicule`) existent déjà. Le travail est **ciblé** : ajouter `province` au conducteur,
introduire un filtrage par Specifications, corriger les autorisations et scoper la lecture.

---

## 2. Préparation Git

### 2.1 Mettre `develop` à jour

```powershell
cd C:\Users\HP\Desktop\srm-fleet
git checkout develop
git pull origin develop
git status   # doit être clean
```

### 2.2 Créer la branche de fonctionnalité

```powershell
git checkout -b feature/conducteurs-province-departement
git branch --show-current
```

**Choix du nom** — convention du projet : `feature/<domaine>-<sujet>` (cf. `feature/conducteurs-permis-backend`
des PR #17/#18). `conducteurs-province-departement` décrit le cœur de l'évolution (rattachement géographique)
qui entraîne mécaniquement le filtrage et la sécurité provinciale. Court, explicite, cohérent avec l'historique.

---

## 3. Développement (ordre logique et pourquoi)

L'ordre suit le flux de la donnée : **schéma → entité → contrat (DTO/validation) → mapping → accès données
(repository + specifications) → logique métier (service) → exposition (controller/sécurité) → tests**.
Chaque couche s'appuie sur la précédente ; l'inverse casserait la compilation.

### Étape 3.1 — Migration base de données (V8)

> Pourquoi d'abord : l'entité doit refléter un schéma qui existe. La table `conducteur` étant vide,
> les colonnes `NOT NULL` sont posables sans backfill.

**`backend/src/main/resources/db/migration/V8__conducteur_province.sql`**

```sql
-- V8 : rattachement du conducteur à une province + département obligatoire (filtrage et portée provinciale)

-- Nouvelle FK vers province (indépendante de departement : aucune relation province<->departement)
ALTER TABLE conducteur
    ADD COLUMN id_province BIGINT NOT NULL REFERENCES province(id_province);

-- Le département devient obligatoire (chaque conducteur est rattaché à un département)
ALTER TABLE conducteur
    ALTER COLUMN id_departement SET NOT NULL;

-- Index de filtrage (liste filtrée par province et/ou département, portée provinciale)
CREATE INDEX idx_conducteur_province    ON conducteur (id_province);
CREATE INDEX idx_conducteur_departement ON conducteur (id_departement);
```

> ⚠️ Si (contrairement au dépôt actuel) des conducteurs existaient déjà, il faudrait d'abord
> renseigner `id_province` sur les lignes existantes avant de poser `NOT NULL`.

### Étape 3.2 — Entité `Conducteur`

> Pourquoi ensuite : le mapping objet-relationnel doit correspondre au nouveau schéma.

Dans **`conducteur/conducteur/Conducteur.java`** :

1. Importer `Province` : `import ma.srm.fleet.referentiel.province.Province;`
2. Ajouter le champ province (aligné sur `Vehicule` : `optional=false`, FK non nulle) :

```java
    /** Obligatoire : sert au filtrage et à la portée provinciale (RG14). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_province", nullable = false)
    private Province province;
```

3. Rendre le département obligatoire (aligné sur la migration) — passer le `@ManyToOne` existant en `optional = false` et `nullable = false` :

```java
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_departement", nullable = false)
    private Departement departement;
```

4. Faire du conducteur l'**agrégat racine** des permis pour que la suppression cascade (nécessaire au nouvel endpoint DELETE) :

```java
    @OneToMany(mappedBy = "conducteur", fetch = FetchType.LAZY,
               cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ma.srm.fleet.conducteur.permis.Permis> permis = new java.util.ArrayList<>();
```

> `cascade = ALL, orphanRemoval = true` : supprimer un conducteur supprime ses permis (sinon violation de FK,
> `permis.id_conducteur` étant `NOT NULL`). Ne change rien à l'ajout unitaire de permis existant.

### Étape 3.3 — DTO + validations

> Pourquoi ici : le contrat d'API et ses contraintes se définissent avant le mapping et le service qui les consomment.

**`conducteur/conducteur/dto/ConducteurRequest.java`** — province et département deviennent obligatoires :

```java
package ma.srm.fleet.conducteur.conducteur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConducteurRequest(
        @NotBlank String matricule,
        @NotBlank String nom,
        @NotBlank String prenom,
        String fonction,
        String groupeSanguin,
        @NotNull Long provinceId,
        @NotNull Long departementId) {}
```

**`conducteur/conducteur/dto/ConducteurResponse.java`** — exposer la province (id + libellé) :

```java
package ma.srm.fleet.conducteur.conducteur.dto;

import java.util.List;
import ma.srm.fleet.conducteur.permis.dto.PermisResponse;

public record ConducteurResponse(
        Long id, String matricule, String nom, String prenom, String fonction, String groupeSanguin,
        Long provinceId, String provinceLibelle,
        Long departementId, String departementLibelle,
        List<PermisResponse> permis) {}
```

**`conducteur/conducteur/dto/ConducteurFilter.java`** — critères de recherche (extensible : un champ par futur critère) :

```java
package ma.srm.fleet.conducteur.conducteur.dto;

/**
 * Critères de filtrage de la liste des conducteurs. Chaque champ est optionnel
 * (null = critère ignoré). Ajouter un futur critère = ajouter un champ ici + une
 * méthode dans ConducteurSpecifications, sans toucher au reste.
 */
public record ConducteurFilter(String recherche, Long provinceId, Long departementId) {}
```

### Étape 3.4 — Specifications (mécanisme de filtrage extensible)

> Pourquoi ici : le service en dépend. On isole chaque critère dans une méthode réutilisable et combinable.

**`conducteur/conducteur/ConducteurSpecifications.java`**

```java
package ma.srm.fleet.conducteur.conducteur;

import org.springframework.data.jpa.domain.Specification;

/**
 * Fabrique de Specifications pour le filtrage des conducteurs.
 *
 * Chaque méthode renvoie un prédicat ; quand le critère est absent, elle renvoie
 * Specification.unrestricted() (toujours vrai) afin d'être composable sans null.
 * Extensible : un nouveau critère = une nouvelle méthode, combinée via allOf(...).
 */
public final class ConducteurSpecifications {

    private ConducteurSpecifications() {}

    /** Recherche plein-texte sur matricule / nom / prénom (insensible à la casse). */
    public static Specification<Conducteur> recherche(String q) {
        if (q == null || q.isBlank()) {
            return Specification.unrestricted();
        }
        String like = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("matricule")), like),
                cb.like(cb.lower(root.get("nom")), like),
                cb.like(cb.lower(root.get("prenom")), like));
    }

    /** Filtre par province (identifiant technique). */
    public static Specification<Conducteur> province(Long provinceId) {
        if (provinceId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("province").get("id"), provinceId);
    }

    /** Filtre par département (identifiant technique). */
    public static Specification<Conducteur> departement(Long departementId) {
        if (departementId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("departement").get("id"), departementId);
    }
}
```

> `Specification.unrestricted()` existe depuis Spring Data JPA 3.2 (le projet est en Boot 3.5) : évite tout `null`.

### Étape 3.5 — Repository

> Pourquoi ici : activer l'exécution de Specifications. On garde `existsByMatricule`, on supprime le `@Query rechercher`
> désormais couvert par `ConducteurSpecifications.recherche`.

**`conducteur/conducteur/ConducteurRepository.java`**

```java
package ma.srm.fleet.conducteur.conducteur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ConducteurRepository
        extends JpaRepository<Conducteur, Long>, JpaSpecificationExecutor<Conducteur> {

    boolean existsByMatricule(String matricule);
}
```

### Étape 3.6 — Service (mapping, portée provinciale, CRUD)

> Pourquoi ici : orchestre repository + specifications + règles métier. La portée provinciale (RG14) est
> appliquée **ici**, côté serveur, indépendamment de l'UI.

**`conducteur/conducteur/ConducteurService.java`** (version complète) :

```java
package ma.srm.fleet.conducteur.conducteur;

import java.util.List;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.conducteur.conducteur.dto.ConducteurFilter;
import ma.srm.fleet.conducteur.conducteur.dto.ConducteurRequest;
import ma.srm.fleet.conducteur.conducteur.dto.ConducteurResponse;
import ma.srm.fleet.conducteur.permis.Permis;
import ma.srm.fleet.conducteur.permis.PermisRepository;
import ma.srm.fleet.conducteur.permis.dto.PermisRequest;
import ma.srm.fleet.conducteur.permis.dto.PermisResponse;
import ma.srm.fleet.referentiel.departement.Departement;
import ma.srm.fleet.referentiel.departement.DepartementRepository;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.province.ProvinceRepository;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConducteurService {

    private final ConducteurRepository conducteurRepository;
    private final PermisRepository permisRepository;
    private final DepartementRepository departementRepository;
    private final ProvinceRepository provinceRepository;

    public ConducteurService(ConducteurRepository conducteurRepository,
                             PermisRepository permisRepository,
                             DepartementRepository departementRepository,
                             ProvinceRepository provinceRepository) {
        this.conducteurRepository = conducteurRepository;
        this.permisRepository = permisRepository;
        this.departementRepository = departementRepository;
        this.provinceRepository = provinceRepository;
    }

    @Transactional(readOnly = true)
    public List<ConducteurResponse> lister(ConducteurFilter filtre, UserPrincipal principal) {
        Specification<Conducteur> spec = Specification.allOf(
                ConducteurSpecifications.recherche(filtre.recherche()),
                ConducteurSpecifications.province(filtre.provinceId()),
                ConducteurSpecifications.departement(filtre.departementId()),
                porteeProvinciale(principal));
        return conducteurRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ConducteurResponse parId(Long id, UserPrincipal principal) {
        Conducteur c = charger(id);
        verifierPortee(c, principal);
        return toResponse(c);
    }

    public ConducteurResponse creer(ConducteurRequest req) {
        if (conducteurRepository.existsByMatricule(req.matricule())) {
            throw new BusinessException("Ce matricule est déjà utilisé", HttpStatus.CONFLICT);
        }
        Conducteur c = new Conducteur();
        appliquer(c, req);
        return toResponse(conducteurRepository.save(c));
    }

    public ConducteurResponse modifier(Long id, ConducteurRequest req) {
        Conducteur c = charger(id);
        if (!c.getMatricule().equals(req.matricule()) && conducteurRepository.existsByMatricule(req.matricule())) {
            throw new BusinessException("Ce matricule est déjà utilisé", HttpStatus.CONFLICT);
        }
        appliquer(c, req);
        return toResponse(c);
    }

    public void supprimer(Long id) {
        Conducteur c = charger(id);
        conducteurRepository.delete(c);   // cascade ALL + orphanRemoval → supprime aussi les permis
    }

    public PermisResponse ajouterPermis(Long conducteurId, PermisRequest req) {
        Conducteur c = charger(conducteurId);
        Permis p = new Permis();
        p.setTypePermis(req.typePermis());
        p.setDateFinValidite(req.dateFinValidite());
        p.setConducteur(c);
        return toPermis(permisRepository.save(p));
    }

    public void supprimerPermis(Long conducteurId, Long permisId) {
        Permis p = permisRepository.findById(permisId)
                .orElseThrow(() -> new BusinessException("Permis introuvable", HttpStatus.NOT_FOUND));
        if (!p.getConducteur().getId().equals(conducteurId)) {
            throw new BusinessException("Ce permis n'appartient pas à ce conducteur", HttpStatus.BAD_REQUEST);
        }
        permisRepository.delete(p);
    }

    // --- Portée provinciale (RG14) : appliquée côté serveur ---

    /** Restreint la liste à la province du Responsable Provincial ; aucune restriction pour l'admin central. */
    private Specification<Conducteur> porteeProvinciale(UserPrincipal principal) {
        return principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL
                ? ConducteurSpecifications.province(principal.getProvinceId())
                : Specification.unrestricted();
    }

    /**
     * Empêche un Responsable Provincial d'accéder à un conducteur hors de sa province.
     * On renvoie 404 (et non 403) pour ne pas révéler l'existence d'un conducteur d'une autre province.
     */
    private void verifierPortee(Conducteur c, UserPrincipal principal) {
        if (principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL
                && !c.getProvince().getId().equals(principal.getProvinceId())) {
            throw new BusinessException("Conducteur introuvable : " + c.getId(), HttpStatus.NOT_FOUND);
        }
    }

    // --- Mapping / résolution des références (mapping inline, cohérent avec UtilisateurService) ---

    private void appliquer(Conducteur c, ConducteurRequest req) {
        c.setMatricule(req.matricule());
        c.setNom(req.nom());
        c.setPrenom(req.prenom());
        c.setFonction(req.fonction());
        c.setGroupeSanguin(req.groupeSanguin());
        c.setProvince(resoudreProvince(req.provinceId()));
        c.setDepartement(resoudreDepartement(req.departementId()));
    }

    private Province resoudreProvince(Long id) {
        return provinceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Province introuvable", HttpStatus.BAD_REQUEST));
    }

    private Departement resoudreDepartement(Long id) {
        return departementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Département introuvable", HttpStatus.BAD_REQUEST));
    }

    private Conducteur charger(Long id) {
        return conducteurRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Conducteur introuvable : " + id, HttpStatus.NOT_FOUND));
    }

    private ConducteurResponse toResponse(Conducteur c) {
        Province p = c.getProvince();
        Departement d = c.getDepartement();
        List<PermisResponse> permis = c.getPermis() == null ? List.of()
                : c.getPermis().stream().map(this::toPermis).toList();
        return new ConducteurResponse(
                c.getId(), c.getMatricule(), c.getNom(), c.getPrenom(), c.getFonction(), c.getGroupeSanguin(),
                p != null ? p.getId() : null, p != null ? p.getLibelle() : null,
                d != null ? d.getId() : null, d != null ? d.getLibelle() : null,
                permis);
    }

    private PermisResponse toPermis(Permis p) {
        return new PermisResponse(p.getId(), p.getTypePermis(), p.getDateFinValidite());
    }
}
```

> **Note portée** : `province`/`departement` étant désormais `optional=false`, `p`/`d` ne seront pas nuls
> en pratique ; les gardes `!= null` restent par prudence (mapping défensif, sans coût).

### Étape 3.7 — Contrôleur + sécurité

> Pourquoi ici : dernière couche exposée. Écritures **réservées à l'admin central** ; lectures scopées via le service.

**`conducteur/conducteur/ConducteurController.java`** (version complète) :

```java
package ma.srm.fleet.conducteur.conducteur;

import jakarta.validation.Valid;
import java.util.List;
import ma.srm.fleet.conducteur.conducteur.dto.ConducteurFilter;
import ma.srm.fleet.conducteur.conducteur.dto.ConducteurRequest;
import ma.srm.fleet.conducteur.conducteur.dto.ConducteurResponse;
import ma.srm.fleet.conducteur.permis.dto.PermisRequest;
import ma.srm.fleet.conducteur.permis.dto.PermisResponse;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/conducteurs")
public class ConducteurController {

    private final ConducteurService service;

    public ConducteurController(ConducteurService service) { this.service = service; }

    // Lecture ouverte aux deux rôles ; la portée provinciale est appliquée dans le service.
    @GetMapping
    public List<ConducteurResponse> lister(
            @RequestParam(required = false) String recherche,
            @RequestParam(required = false) Long provinceId,
            @RequestParam(required = false) Long departementId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return service.lister(new ConducteurFilter(recherche, provinceId, departementId), principal);
    }

    @GetMapping("/{id}")
    public ConducteurResponse parId(@PathVariable Long id,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return service.parId(id, principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public ConducteurResponse creer(@Valid @RequestBody ConducteurRequest req) {
        return service.creer(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public ConducteurResponse modifier(@PathVariable Long id, @Valid @RequestBody ConducteurRequest req) {
        return service.modifier(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }

    @PostMapping("/{id}/permis")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public PermisResponse ajouterPermis(@PathVariable Long id, @Valid @RequestBody PermisRequest req) {
        return service.ajouterPermis(id, req);
    }

    @DeleteMapping("/{id}/permis/{permisId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRATEUR_CENTRAL')")
    public void supprimerPermis(@PathVariable Long id, @PathVariable Long permisId) {
        service.supprimerPermis(id, permisId);
    }
}
```

**Ce qui change côté sécurité :**
- Toutes les écritures : `hasAnyRole('ADMINISTRATEUR_CENTRAL','RESPONSABLE_PROVINCIAL')` → **`hasRole('ADMINISTRATEUR_CENTRAL')`**.
- Nouveau `DELETE /{id}` (droit « supprimer un conducteur » de l'admin central).
- `lister`/`parId` reçoivent le `UserPrincipal` pour appliquer la portée provinciale dans le service.

### Étape 3.8 — Tests

> Pourquoi en dernier : on teste un comportement stabilisé. Test unitaire Mockito du service (style du projet),
> centré sur les règles métier et de sécurité.

**`backend/src/test/java/ma/srm/fleet/conducteur/conducteur/ConducteurServiceTest.java`**

```java
package ma.srm.fleet.conducteur.conducteur;

import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.conducteur.conducteur.dto.ConducteurRequest;
import ma.srm.fleet.conducteur.permis.PermisRepository;
import ma.srm.fleet.referentiel.departement.Departement;
import ma.srm.fleet.referentiel.departement.DepartementRepository;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.province.ProvinceRepository;
import ma.srm.fleet.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConducteurServiceTest {

    @Mock ConducteurRepository conducteurRepository;
    @Mock PermisRepository permisRepository;
    @Mock DepartementRepository departementRepository;
    @Mock ProvinceRepository provinceRepository;
    @InjectMocks ConducteurService service;

    private UserPrincipal principal(RoleUtilisateur role, Long provinceId) {
        UserPrincipal p = mock(UserPrincipal.class);
        lenient().when(p.getRole()).thenReturn(role);
        lenient().when(p.getProvinceId()).thenReturn(provinceId);
        return p;
    }

    private Conducteur conducteurAvecProvince(Long id, Long provinceId) {
        Province prov = new Province();
        prov.setId(provinceId);
        Conducteur c = new Conducteur();
        c.setId(id);
        c.setMatricule("C001");
        c.setProvince(prov);
        return c;
    }

    @Test
    void creer_matriculeExistant_leve409() {
        ConducteurRequest req = new ConducteurRequest("C001", "Bennani", "Youssef", null, null, 1L, 2L);
        when(conducteurRepository.existsByMatricule("C001")).thenReturn(true);

        assertThatThrownBy(() -> service.creer(req))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void creer_provinceIntrouvable_leve400() {
        ConducteurRequest req = new ConducteurRequest("C001", "Bennani", "Youssef", null, null, 99L, 2L);
        when(conducteurRepository.existsByMatricule("C001")).thenReturn(false);
        when(provinceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.creer(req))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void parId_responsableProvincial_horsSaProvince_leve404() {
        Conducteur c = conducteurAvecProvince(10L, 1L);           // conducteur en province 1
        when(conducteurRepository.findById(10L)).thenReturn(Optional.of(c));
        UserPrincipal resp = principal(RoleUtilisateur.RESPONSABLE_PROVINCIAL, 2L);  // responsable province 2

        assertThatThrownBy(() -> service.parId(10L, resp))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void parId_responsableProvincial_dansSaProvince_ok() {
        Conducteur c = conducteurAvecProvince(10L, 2L);
        when(conducteurRepository.findById(10L)).thenReturn(Optional.of(c));
        UserPrincipal resp = principal(RoleUtilisateur.RESPONSABLE_PROVINCIAL, 2L);

        assertThat(service.parId(10L, resp).id()).isEqualTo(10L);
    }

    @Test
    void parId_adminCentral_accedeAToutesLesProvinces() {
        Conducteur c = conducteurAvecProvince(10L, 1L);
        when(conducteurRepository.findById(10L)).thenReturn(Optional.of(c));
        UserPrincipal admin = principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null);

        assertThat(service.parId(10L, admin).id()).isEqualTo(10L);
    }

    @Test
    void supprimerPermis_dUnAutreConducteur_leve400() {
        var permis = new ma.srm.fleet.conducteur.permis.Permis();
        var autre = conducteurAvecProvince(2L, 1L);
        permis.setConducteur(autre);
        when(permisRepository.findById(5L)).thenReturn(Optional.of(permis));

        assertThatThrownBy(() -> service.supprimerPermis(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
```

> Le filtrage de portée de `lister` (Specifications) et les `@PreAuthorize` sont mieux couverts par la
> **vérification REST manuelle** ci-dessous (deux tokens). Un test d'intégration `@SpringBootTest` reste un
> bon *follow-up* si l'équipe veut automatiser le 403.

---

## 4. Vérifications avant chaque commit

À exécuter **avant** de committer :

```powershell
cd backend
mvn -q clean verify        # compilation + tests
cd ..
```

1. **Compilation** : `mvn clean verify` vert (aucune erreur de compilation, Flyway V8 applicable).
2. **Tests** : `ConducteurServiceTest` + les tests existants passent.
3. **Règles métier** — vérification REST manuelle (`make up` puis) :

```powershell
# --- Token admin central ---
$admin = (Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method Post -ContentType "application/json" -Body (@{login="admin";motDePasse="Admin@123"}|ConvertTo-Json)).token
$ha = @{ Authorization = "Bearer $admin" }

# Création (province + département obligatoires) → 201
$body = @{ matricule="C001"; nom="Bennani"; prenom="Youssef"; groupeSanguin="O+"; provinceId=1; departementId=1 } | ConvertTo-Json
$c = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/conducteurs" -Method Post -Headers $ha -ContentType "application/json" -Body $body

# Filtrage
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/conducteurs?provinceId=1" -Headers $ha
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/conducteurs?provinceId=1&departementId=1" -Headers $ha

# Suppression conducteur (cascade permis) → 204
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/conducteurs/$($c.id)" -Method Delete -Headers $ha
```

   Pour tester la **portée provinciale** et le **403**, crée (via l'admin) un `RESPONSABLE_PROVINCIAL`
   rattaché à une province, connecte-toi avec, puis vérifie :
   - `GET /conducteurs` → ne renvoie que les conducteurs de **sa** province ;
   - `GET /conducteurs/{idAutreProvince}` → **404** ;
   - `POST /conducteurs` → **403** ; `PUT`, `DELETE`, `.../permis` → **403**.

4. **Revue rapide** : aucun `RESPONSABLE_PROVINCIAL` restant dans les `@PreAuthorize` d'écriture ;
   aucune relation ajoutée entre `Province` et `Departement` ; pas de code mort (ancien `@Query rechercher` supprimé).

---

## 5. Git — commits

Découpage en commits cohérents (conventional commits, scope `conducteurs`, comme l'historique) :

```powershell
# 1) Rattachement géographique : schéma, entité, contrat
git add backend/src/main/resources/db/migration/V8__conducteur_province.sql `
        backend/src/main/java/ma/srm/fleet/conducteur/conducteur/Conducteur.java `
        backend/src/main/java/ma/srm/fleet/conducteur/conducteur/dto/ConducteurRequest.java `
        backend/src/main/java/ma/srm/fleet/conducteur/conducteur/dto/ConducteurResponse.java
git commit -m "feat(conducteurs): rattachement province et departement obligatoires"

# 2) Filtrage extensible + portée provinciale (RG14)
git add backend/src/main/java/ma/srm/fleet/conducteur/conducteur/dto/ConducteurFilter.java `
        backend/src/main/java/ma/srm/fleet/conducteur/conducteur/ConducteurSpecifications.java `
        backend/src/main/java/ma/srm/fleet/conducteur/conducteur/ConducteurRepository.java `
        backend/src/main/java/ma/srm/fleet/conducteur/conducteur/ConducteurService.java
git commit -m "feat(conducteurs): filtrage province/departement et portee provinciale (RG14)"

# 3) Autorisations : ecritures reservees a l'admin central + DELETE conducteur
git add backend/src/main/java/ma/srm/fleet/conducteur/conducteur/ConducteurController.java
git commit -m "fix(conducteurs): ecritures reservees a l'administrateur central + suppression conducteur"

# 4) Tests
git add backend/src/test/java/ma/srm/fleet/conducteur/conducteur/ConducteurServiceTest.java
git commit -m "test(conducteurs): unicite matricule et portee provinciale"

git push -u origin feature/conducteurs-province-departement
```

> Si tu préfères un commit unique, garde le message n°2 comme titre principal et cite les autres en corps.

---

## 6. Description de la Pull Request (à coller dans GitHub)

```markdown
## Contexte
Le référentiel conducteurs (RF-05/RF-06) évolue : chaque conducteur est désormais rattaché à une
**Province** et un **Département** (indépendants, aucune relation entre ces deux tables), afin de permettre
le **filtrage** par province / département / les deux, et d'appliquer une **portée provinciale** conforme
au modèle de droits du projet (RG14).

## Changements apportés
- **BDD (V8)** : `conducteur.id_province` (FK `province`, NOT NULL) + `id_departement` passé NOT NULL ;
  index sur les deux colonnes pour le filtrage.
- **Entité** : `Conducteur.province` (@ManyToOne obligatoire) ; département obligatoire ; permis en
  `cascade=ALL, orphanRemoval=true` (agrégat racine → suppression en cascade).
- **DTO/validation** : `provinceId`/`departementId` obligatoires (`@NotNull`) ; `ConducteurResponse`
  expose la province ; nouveau `ConducteurFilter`.
- **Filtrage** : `ConducteurSpecifications` (recherche + province + département), combinables et extensibles ;
  `ConducteurRepository` étend `JpaSpecificationExecutor`.
- **Service** : portée provinciale appliquée côté serveur (liste filtrée, accès direct hors province → 404) ;
  nouvelle suppression de conducteur.
- **Sécurité** : écritures (`POST/PUT/DELETE`, gestion des permis) **réservées à l'ADMINISTRATEUR_CENTRAL** ;
  nouveau `DELETE /conducteurs/{id}`.
- **Nettoyage** : suppression du `@Query rechercher` (remplacé par une Specification).

## Impacts
- **Rupture de contrat API** : `provinceId` devient obligatoire à la création/modification d'un conducteur
  (le frontend doit envoyer une province). `ConducteurResponse` gagne `provinceId`/`provinceLibelle`.
- Un **Responsable Provincial** ne peut plus créer/modifier/supprimer de conducteur (403) et ne voit que
  les conducteurs de sa province.

## Règles métier implémentées
- Province et département obligatoires, **sans** relation entre eux.
- Admin Central : CRUD complet, visibilité nationale.
- Responsable Provincial : lecture seule, limitée à sa province (403 en écriture, 404 hors province).
- Filtrage province / département / combiné, extensible via Specifications.

## Tests réalisés
- Unitaires (`ConducteurServiceTest`) : matricule dupliqué → 409, province introuvable → 400,
  portée provinciale (404 hors province, OK dans sa province, admin voit tout), permis d'un autre
  conducteur → 400.
- Manuels (REST, 2 tokens) : filtrage province/département, 403 en écriture pour le responsable,
  404 sur conducteur hors province, suppression en cascade des permis.

## Points d'attention pour le reviewer
- Vérifier qu'**aucune** relation n'a été introduite entre `Province` et `Departement`.
- Vérifier que plus aucun `@PreAuthorize` d'écriture ne mentionne `RESPONSABLE_PROVINCIAL`.
- La portée provinciale est **côté service** (pas seulement UI) : voir `porteeProvinciale` / `verifierPortee`.
- Migration V8 : la table `conducteur` étant vide, `NOT NULL` est posé sans backfill.
```

---

## 7. Checklist finale (avant merge)

- [ ] `mvn clean verify` vert (compilation + tests).
- [ ] Migration Flyway **V8** appliquée proprement (`make up`, logs sans erreur).
- [ ] Province **et** département obligatoires à la création (400 si absents).
- [ ] **Aucune** relation Province ↔ Département introduite.
- [ ] Écritures conducteurs/permis **réservées à l'admin central** (403 pour le responsable provincial).
- [ ] Responsable Provincial : liste limitée à sa province ; accès direct hors province → 404.
- [ ] Filtrage `provinceId`, `departementId` et combinaison fonctionnels.
- [ ] Endpoint `DELETE /conducteurs/{id}` supprime le conducteur **et** ses permis (cascade).
- [ ] Code mort supprimé (ancien `@Query rechercher`).
- [ ] Conventions respectées (injection par constructeur, mapping inline, RFC 7807, commits `type(scope):`).
- [ ] Documentation à jour (ce guide + description de PR).

---

## Annexe (optionnelle) — Frontend : exposer les filtres

Le backend suffit à « préparer le filtrage » (objectif du besoin). Pour l'exploiter dans l'UI, une PR
frontend séparée (`feature/conducteurs-filtres-frontend`) suffit :

1. **`conducteur.models.ts`** : ajouter `provinceId`/`provinceLibelle` à `Conducteur` et
   `provinceId: number | null` à `ConducteurRequest`.
2. **`conducteur.service.ts`** : étendre `lister` avec `provinceId?` / `departementId?` (via `HttpParams`).
3. **`conducteur-list.component.ts`** : deux `<select>` (province, département) alimentés par les endpoints
   `GET /api/v1/provinces` et `GET /api/v1/departements` (déjà ouverts en lecture), relançant `charger()`.
   Masquer « Nouveau conducteur » / actions d'écriture si le rôle courant n'est pas `ADMINISTRATEUR_CENTRAL`
   (via `auth.service`).
4. **`conducteur-form.component.ts`** : ajouter le `<select>` province (obligatoire) à côté du département.

> Respecter `DESIGN_SYSTEM.md` (primitives `.field`/`.input`/`.select`, `OnPush` + signals, états).
