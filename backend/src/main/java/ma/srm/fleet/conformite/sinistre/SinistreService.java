package ma.srm.fleet.conformite.sinistre;

import java.util.List;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.affectation.Affectation;
import ma.srm.fleet.affectation.AffectationRepository;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.conformite.sinistre.dto.SinistreRequest;
import ma.srm.fleet.conformite.sinistre.dto.SinistreResponse;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.vehicule.Vehicule;
import ma.srm.fleet.referentiel.vehicule.VehiculeRepository;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SinistreService {

    private final SinistreRepository sinistreRepository;
    private final VehiculeRepository vehiculeRepository;
    private final AffectationRepository affectationRepository;

    public SinistreService(SinistreRepository sinistreRepository,
                           VehiculeRepository vehiculeRepository,
                           AffectationRepository affectationRepository) {
        this.sinistreRepository = sinistreRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.affectationRepository = affectationRepository;
    }

    /**
     * RF-23 : déclarer un sinistre. Il est rattaché au véhicule et, si elle existe (RG7), à
     * l'affectation active au moment des faits — résolue automatiquement pour identifier sans
     * ambiguïté le conducteur responsable. Le sinistre naît au statut OUVERT.
     */
    public SinistreResponse creer(SinistreRequest req, UserPrincipal principal) {
        Vehicule v = chargerVehicule(req.vehiculeId(), principal);
        Affectation affectation = affectationRepository.findCouvrantDate(v.getId(), req.dateSinistre()).stream()
            .findFirst().orElse(null);   // RG7 : NULL si aucune affectation ne couvre la date

        Sinistre s = new Sinistre();
        s.setVehicule(v);
        s.setAffectation(affectation);
        s.setDateSinistre(req.dateSinistre());
        s.setDescription(req.description());
        s.setStatut(StatutSinistre.OUVERT);
        return SinistreMapper.toResponse(sinistreRepository.save(s));
    }

    /** RF-24 : avancer le statut en respectant l'ordre ouvert → en_cours → clôturé. */
    public SinistreResponse changerStatut(Long id, StatutSinistre cible, UserPrincipal principal) {
        Sinistre s = charger(id, principal);
        verifierTransition(s.getStatut(), cible);
        s.setStatut(cible);
        return SinistreMapper.toResponse(s);
    }

    @Transactional(readOnly = true)
    public List<SinistreResponse> lister(StatutSinistre statut, Long vehiculeId, UserPrincipal principal) {
        Specification<Sinistre> spec = Specification.allOf(
            SinistreSpecifications.statut(statut),
            SinistreSpecifications.vehicule(vehiculeId));
        if (principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL) {
            if (principal.getProvinceId() == null) return List.of();
            spec = spec.and(SinistreSpecifications.province(principal.getProvinceId()));
        }
        return sinistreRepository.findAll(spec).stream().map(SinistreMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SinistreResponse parId(Long id, UserPrincipal principal) {
        return SinistreMapper.toResponse(charger(id, principal));
    }

    /** Transitions autorisées : OUVERT → EN_COURS → CLOTURE, une étape à la fois. */
    private void verifierTransition(StatutSinistre courant, StatutSinistre cible) {
        boolean ok = (courant == StatutSinistre.OUVERT && cible == StatutSinistre.EN_COURS)
            || (courant == StatutSinistre.EN_COURS && cible == StatutSinistre.CLOTURE);
        if (!ok) {
            throw new BusinessException(
                "Transition de statut invalide : " + courant + " → " + cible
                    + " (ordre attendu : ouvert → en_cours → clôturé)",
                HttpStatus.CONFLICT);
        }
    }

    /** Charge un sinistre en masquant son existence à un responsable hors province (404). */
    private Sinistre charger(Long id, UserPrincipal principal) {
        Sinistre s = sinistreRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Sinistre introuvable : " + id, HttpStatus.NOT_FOUND));
        verifierPortee(s.getVehicule(), principal, "Sinistre introuvable : " + id);
        return s;
    }

    private Vehicule chargerVehicule(Long vehiculeId, UserPrincipal principal) {
        Vehicule v = vehiculeRepository.findById(vehiculeId)
            .orElseThrow(() -> new BusinessException("Véhicule introuvable : " + vehiculeId, HttpStatus.BAD_REQUEST));
        verifierPortee(v, principal, "Véhicule introuvable : " + vehiculeId);
        return v;
    }

    private void verifierPortee(Vehicule v, UserPrincipal principal, String messageSiHorsPortee) {
        if (principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL) {
            Province prov = v.getProvince();
            if (prov == null || !prov.getId().equals(principal.getProvinceId())) {
                throw new BusinessException(messageSiHorsPortee, HttpStatus.NOT_FOUND);
            }
        }
    }
}
