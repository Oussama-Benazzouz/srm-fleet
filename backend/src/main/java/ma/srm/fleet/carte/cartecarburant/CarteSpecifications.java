package ma.srm.fleet.carte.cartecarburant;

import org.springframework.data.jpa.domain.Specification;

public final class CarteSpecifications {

    private CarteSpecifications() {}

    public static Specification<CarteCarburant> statut(StatutCarte s) {
        return (root, q, cb) -> s == null ? null : cb.equal(root.get("statut"), s);
    }

    public static Specification<CarteCarburant> affectation(Long affectationId) {
        return (root, q, cb) -> affectationId == null ? null
            : cb.equal(root.get("affectation").get("id"), affectationId);
    }

    /** Portée provinciale : via la province du véhicule de l'affectation. */
    public static Specification<CarteCarburant> province(Long provinceId) {
        return (root, q, cb) -> provinceId == null ? null
            : cb.equal(root.get("affectation").get("vehicule").get("province").get("id"), provinceId);
    }
}
