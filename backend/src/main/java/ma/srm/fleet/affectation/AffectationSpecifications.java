package ma.srm.fleet.affectation;

import org.springframework.data.jpa.domain.Specification;

public final class AffectationSpecifications {

    private AffectationSpecifications() {}

    public static Specification<Affectation> statut(StatutAffectation s) {
        return (root, q, cb) -> s == null ? null : cb.equal(root.get("statut"), s);
    }

    public static Specification<Affectation> vehicule(Long vehiculeId) {
        return (root, q, cb) -> vehiculeId == null ? null : cb.equal(root.get("vehicule").get("id"), vehiculeId);
    }

    public static Specification<Affectation> conducteur(Long conducteurId) {
        return (root, q, cb) -> conducteurId == null ? null : cb.equal(root.get("conducteur").get("id"), conducteurId);
    }

    /** Portée provinciale : via la province du véhicule. */
    public static Specification<Affectation> province(Long provinceId) {
        return (root, q, cb) -> provinceId == null ? null
            : cb.equal(root.get("vehicule").get("province").get("id"), provinceId);
    }
}
