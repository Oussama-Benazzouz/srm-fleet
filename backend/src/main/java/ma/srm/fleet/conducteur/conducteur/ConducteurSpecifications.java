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
