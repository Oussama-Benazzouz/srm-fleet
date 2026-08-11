package ma.srm.fleet.affectation;

import ma.srm.fleet.affectation.dto.AffectationResponse;
import ma.srm.fleet.conducteur.conducteur.Conducteur;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.vehicule.Vehicule;

final class AffectationMapper {

    private AffectationMapper() {}

    static AffectationResponse toResponse(Affectation a) {
        Vehicule v = a.getVehicule();
        Conducteur c = a.getConducteur();
        Province p = v.getProvince();
        return new AffectationResponse(
            a.getId(),
            v.getId(), v.getImmatriculation(),
            c.getId(), c.getPrenom() + " " + c.getNom(), c.getMatricule(),
            p != null ? p.getId() : null, p != null ? p.getLibelle() : null,
            a.getDateDebut(), a.getDateFin(), a.getStatut(), a.getMotif());
    }
}
