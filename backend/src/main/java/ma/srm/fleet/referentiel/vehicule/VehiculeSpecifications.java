package ma.srm.fleet.referentiel.vehicule;

import org.springframework.data.jpa.domain.Specification;

public final class VehiculeSpecifications {

    private VehiculeSpecifications() {}

    public static Specification<Vehicule> immatriculationContient(String v) {
        return (root, query, cb) -> (v == null || v.isBlank())
            ? null : cb.like(cb.lower(root.get("immatriculation")), "%" + v.toLowerCase() + "%");
    }

    public static Specification<Vehicule> typeContient(String v) {
        return (root, query, cb) -> (v == null || v.isBlank())
            ? null : cb.like(cb.lower(root.get("typeVehicule")), "%" + v.toLowerCase() + "%");
    }

    public static Specification<Vehicule> statutEgal(StatutVehicule s) {
        return (root, query, cb) -> s == null ? null : cb.equal(root.get("statut"), s);
    }

    public static Specification<Vehicule> provinceEgale(Long provinceId) {
        return (root, query, cb) -> provinceId == null ? null : cb.equal(root.get("province").get("id"), provinceId);
    }
}
