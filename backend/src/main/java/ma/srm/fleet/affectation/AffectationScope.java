package ma.srm.fleet.affectation;

import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.conducteur.conducteur.Conducteur;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.vehicule.Vehicule;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.http.HttpStatus;

/** Portée provinciale des affectations (alignée sur l'évolution conducteurs). */
final class AffectationScope {

    private AffectationScope() {}

    /** À la création : le responsable provincial ne peut affecter que dans sa province (véhicule ET conducteur). */
    static void verifierCreation(Vehicule v, UserPrincipal principal) {
        if (principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL
            && (v.getProvince() == null || !v.getProvince().getId().equals(principal.getProvinceId()))) {
            throw new BusinessException("Véhicule introuvable", HttpStatus.NOT_FOUND);
        }
    }

    /** À l'accès/clôture : un responsable ne peut agir que sur une affectation de sa province.
     *  On renvoie 404 pour ne pas révéler l'existence d'une affectation hors province. */
    static void verifier(Affectation a, UserPrincipal principal) {
        if (principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL) {
            Long prov = principal.getProvinceId();
            if (a.getVehicule().getProvince() == null || !a.getVehicule().getProvince().getId().equals(prov)) {
                throw new BusinessException("Affectation introuvable : " + a.getId(), HttpStatus.NOT_FOUND);
            }
        }
    }

    static void verifierProvince(Province province, UserPrincipal principal) {
        if (principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL
            && (province == null || !province.getId().equals(principal.getProvinceId()))) {
            throw new BusinessException("Ressource introuvable", HttpStatus.NOT_FOUND);
        }
    }
}
