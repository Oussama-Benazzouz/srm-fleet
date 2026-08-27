package ma.srm.fleet.conformite.sinistre;

import ma.srm.fleet.affectation.Affectation;
import ma.srm.fleet.conducteur.conducteur.Conducteur;
import ma.srm.fleet.conformite.sinistre.dto.SinistreResponse;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.vehicule.Vehicule;

final class SinistreMapper {

    private SinistreMapper() {}

    static SinistreResponse toResponse(Sinistre s) {
        Vehicule v = s.getVehicule();
        Province p = v.getProvince();
        Affectation a = s.getAffectation();
        Conducteur c = a != null ? a.getConducteur() : null;
        return new SinistreResponse(
            s.getId(),
            v.getId(), v.getImmatriculation(),
            p != null ? p.getId() : null, p != null ? p.getLibelle() : null,
            s.getDateSinistre(), s.getDescription(), s.getStatut(),
            a != null ? a.getId() : null,
            c != null ? c.getId() : null,
            c != null ? c.getPrenom() + " " + c.getNom() : null);
    }
}
