package ma.srm.fleet.conformite.visitetechnique;

import java.util.List;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.affectation.Affectation;
import ma.srm.fleet.affectation.AffectationRepository;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.conducteur.conducteur.Conducteur;
import ma.srm.fleet.conformite.visitetechnique.dto.VisiteRequest;
import ma.srm.fleet.conformite.visitetechnique.dto.VisiteResponse;
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
public class VisiteTechniqueService {

    private final VisiteTechniqueRepository visiteRepository;
    private final VehiculeRepository vehiculeRepository;
    private final AffectationRepository affectationRepository;

    public VisiteTechniqueService(VisiteTechniqueRepository visiteRepository,
                                  VehiculeRepository vehiculeRepository,
                                  AffectationRepository affectationRepository) {
        this.visiteRepository = visiteRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.affectationRepository = affectationRepository;
    }

    /**
     * RF-19 : enregistrer une visite technique. Le conducteur accompagnateur (RG10/RG11) est
     * dérivé de l'affectation couvrant la date de visite : il ne peut donc jamais diverger du
     * titulaire réel. Sans affectation à cette date, la visite ne peut être enregistrée.
     */
    public VisiteResponse creer(VisiteRequest req, UserPrincipal principal) {
        Vehicule v = chargerVehicule(req.vehiculeId(), principal);
        if (req.dateEcheance().isBefore(req.dateVisite())) {
            throw new BusinessException(
                "La date d'échéance doit être postérieure ou égale à la date de visite", HttpStatus.BAD_REQUEST);
        }
        Conducteur accompagnateur = affectationRepository.findCouvrantDate(v.getId(), req.dateVisite()).stream()
            .findFirst()
            .map(Affectation::getConducteur)
            .orElseThrow(() -> new BusinessException(
                "Aucune affectation ne couvre la date de visite : "
                    + "le conducteur accompagnateur ne peut être déterminé (RG11)",
                HttpStatus.CONFLICT));

        VisiteTechnique visite = new VisiteTechnique();
        visite.setVehicule(v);
        visite.setDateVisite(req.dateVisite());
        visite.setDateEcheance(req.dateEcheance());
        visite.setNomCompagnie(req.nomCompagnie());
        visite.setConducteurAccompagnateur(accompagnateur);
        return VisiteTechniqueMapper.toResponse(visiteRepository.save(visite));
    }

    @Transactional(readOnly = true)
    public List<VisiteResponse> lister(Long vehiculeId, UserPrincipal principal) {
        Specification<VisiteTechnique> spec = Specification.allOf(VisiteTechniqueSpecifications.vehicule(vehiculeId));
        if (principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL) {
            if (principal.getProvinceId() == null) return List.of();
            spec = spec.and(VisiteTechniqueSpecifications.province(principal.getProvinceId()));
        }
        return visiteRepository.findAll(spec).stream().map(VisiteTechniqueMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VisiteResponse parId(Long id, UserPrincipal principal) {
        VisiteTechnique visite = visiteRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Visite technique introuvable : " + id, HttpStatus.NOT_FOUND));
        verifierPortee(visite.getVehicule(), principal, "Visite technique introuvable : " + id);
        return VisiteTechniqueMapper.toResponse(visite);
    }

    /** Charge le véhicule cible en masquant son existence à un responsable hors province (404). */
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
