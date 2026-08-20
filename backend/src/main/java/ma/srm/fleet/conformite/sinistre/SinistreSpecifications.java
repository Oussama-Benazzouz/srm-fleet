package ma.srm.fleet.conformite.sinistre;

import org.springframework.data.jpa.domain.Specification;

public final class SinistreSpecifications {

    private SinistreSpecifications() {}

    public static Specification<Sinistre> statut(StatutSinistre statut) {
        return (root, q, cb) -> statut == null ? null : cb.equal(root.get("statut"), statut);
    }

    public static Specification<Sinistre> vehicule(Long vehiculeId) {
        return (root, q, cb) -> vehiculeId == null ? null : cb.equal(root.get("vehicule").get("id"), vehiculeId);
    }

    /** Portée provinciale : via la province du véhicule. */
    public static Specification<Sinistre> province(Long provinceId) {
        return (root, q, cb) -> provinceId == null ? null
            : cb.equal(root.get("vehicule").get("province").get("id"), provinceId);
    }
}
