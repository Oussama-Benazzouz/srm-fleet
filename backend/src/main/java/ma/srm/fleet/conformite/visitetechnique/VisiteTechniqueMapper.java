package ma.srm.fleet.conformite.visitetechnique;

import ma.srm.fleet.conducteur.conducteur.Conducteur;
import ma.srm.fleet.conformite.visitetechnique.dto.VisiteResponse;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.vehicule.Vehicule;

final class VisiteTechniqueMapper {

    private VisiteTechniqueMapper() {}

    static VisiteResponse toResponse(VisiteTechnique visite) {
        Vehicule v = visite.getVehicule();
        Province p = v.getProvince();
        Conducteur c = visite.getConducteurAccompagnateur();
        return new VisiteResponse(
            visite.getId(),
            v.getId(), v.getImmatriculation(),
            p != null ? p.getId() : null, p != null ? p.getLibelle() : null,
            visite.getDateVisite(), visite.getDateEcheance(), visite.getNomCompagnie(),
            c.getId(), c.getPrenom() + " " + c.getNom(), c.getMatricule());
    }
}
