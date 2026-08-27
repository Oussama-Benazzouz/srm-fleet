package ma.srm.fleet.conformite.assurance;

import ma.srm.fleet.conformite.assurance.dto.AssuranceResponse;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.vehicule.Vehicule;

final class AssuranceMapper {

    private AssuranceMapper() {}

    static AssuranceResponse toResponse(Assurance a) {
        Vehicule v = a.getVehicule();
        Province p = v.getProvince();
        return new AssuranceResponse(
            a.getId(),
            v.getId(), v.getImmatriculation(),
            p != null ? p.getId() : null, p != null ? p.getLibelle() : null,
            a.getNomCompagnie(), a.getDateDebut(), a.getDateEcheance());
    }
}
