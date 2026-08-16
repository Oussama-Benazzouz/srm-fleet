package ma.srm.fleet.carte.cartecarburant;

import java.util.List;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.affectation.Affectation;
import ma.srm.fleet.carte.cartecarburant.dto.CarteResponse;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.referentiel.fournisseur.Fournisseur;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.vehicule.Vehicule;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CarteService {

    private final CarteCarburantRepository carteRepository;

    public CarteService(CarteCarburantRepository carteRepository) {
        this.carteRepository = carteRepository;
    }

    @Transactional(readOnly = true)
    public List<CarteResponse> lister(StatutCarte statut, Long affectationId, UserPrincipal principal) {
        Specification<CarteCarburant> spec = Specification.allOf(
            CarteSpecifications.statut(statut),
            CarteSpecifications.affectation(affectationId));
        if (principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL) {
            if (principal.getProvinceId() == null) return List.of();
            spec = spec.and(CarteSpecifications.province(principal.getProvinceId()));
        }
        return carteRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CarteResponse parId(Long id, UserPrincipal principal) {
        CarteCarburant c = charger(id);
        verifierPortee(c, principal);
        return toResponse(c);
    }

    /** Clôture (traçabilité) : ACTIVE → CLOTUREE. Non réversible (on redemande une carte). */
    public CarteResponse cloturer(Long id) {
        CarteCarburant c = charger(id);
        if (c.getStatut() == StatutCarte.CLOTUREE) {
            throw new BusinessException("La carte est déjà clôturée", HttpStatus.CONFLICT);
        }
        c.setStatut(StatutCarte.CLOTUREE);
        return toResponse(c);
    }

    private void verifierPortee(CarteCarburant c, UserPrincipal principal) {
        if (principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL) {
            Province prov = c.getAffectation().getVehicule().getProvince();
            if (prov == null || !prov.getId().equals(principal.getProvinceId())) {
                throw new BusinessException("Carte introuvable : " + c.getId(), HttpStatus.NOT_FOUND);
            }
        }
    }

    private CarteCarburant charger(Long id) {
        return carteRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Carte introuvable : " + id, HttpStatus.NOT_FOUND));
    }

    private CarteResponse toResponse(CarteCarburant c) {
        Affectation a = c.getAffectation();
        Vehicule v = a.getVehicule();
        Province p = v.getProvince();
        Fournisseur f = c.getFournisseur();
        return new CarteResponse(
            c.getId(), c.getNumeroCarte(), c.getPlafondMontant(), c.getStatut(),
            a.getId(), v.getId(), v.getImmatriculation(),
            p != null ? p.getId() : null, p != null ? p.getLibelle() : null,
            f.getId(), f.getRaisonSociale(),
            c.getDemandeOrigine().getId(), c.getDateCreation());
    }
}
