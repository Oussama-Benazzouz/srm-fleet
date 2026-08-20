package ma.srm.fleet.conformite.visitetechnique;

import org.springframework.data.jpa.domain.Specification;

public final class VisiteTechniqueSpecifications {

    private VisiteTechniqueSpecifications() {}

    public static Specification<VisiteTechnique> vehicule(Long vehiculeId) {
        return (root, q, cb) -> vehiculeId == null ? null : cb.equal(root.get("vehicule").get("id"), vehiculeId);
    }

    /** Portée provinciale : via la province du véhicule. */
    public static Specification<VisiteTechnique> province(Long provinceId) {
        return (root, q, cb) -> provinceId == null ? null
            : cb.equal(root.get("vehicule").get("province").get("id"), provinceId);
    }
}
