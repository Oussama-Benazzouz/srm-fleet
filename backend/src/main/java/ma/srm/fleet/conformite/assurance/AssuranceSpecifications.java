package ma.srm.fleet.conformite.assurance;

import org.springframework.data.jpa.domain.Specification;

public final class AssuranceSpecifications {

    private AssuranceSpecifications() {}

    public static Specification<Assurance> vehicule(Long vehiculeId) {
        return (root, q, cb) -> vehiculeId == null ? null : cb.equal(root.get("vehicule").get("id"), vehiculeId);
    }

    /** Portée provinciale : via la province du véhicule. */
    public static Specification<Assurance> province(Long provinceId) {
        return (root, q, cb) -> provinceId == null ? null
            : cb.equal(root.get("vehicule").get("province").get("id"), provinceId);
    }
}
