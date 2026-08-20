package ma.srm.fleet.conformite.assurance;

import java.util.List;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.conformite.assurance.dto.AssuranceRequest;
import ma.srm.fleet.conformite.assurance.dto.AssuranceResponse;
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
public class AssuranceService {

    private final AssuranceRepository assuranceRepository;
    private final VehiculeRepository vehiculeRepository;

    public AssuranceService(AssuranceRepository assuranceRepository, VehiculeRepository vehiculeRepository) {
        this.assuranceRepository = assuranceRepository;
        this.vehiculeRepository = vehiculeRepository;
    }

    /** RF-18 : enregistrer une échéance d'assurance, scopée par province pour un provincial. */
    public AssuranceResponse creer(AssuranceRequest req, UserPrincipal principal) {
        Vehicule v = chargerVehicule(req.vehiculeId(), principal);
        if (req.dateEcheance().isBefore(req.dateDebut())) {
            throw new BusinessException(
                "La date d'échéance doit être postérieure ou égale à la date de début", HttpStatus.BAD_REQUEST);
        }
        Assurance a = new Assurance();
        a.setVehicule(v);
        a.setNomCompagnie(req.nomCompagnie());
        a.setDateDebut(req.dateDebut());
        a.setDateEcheance(req.dateEcheance());
        return AssuranceMapper.toResponse(assuranceRepository.save(a));
    }

    @Transactional(readOnly = true)
    public List<AssuranceResponse> lister(Long vehiculeId, UserPrincipal principal) {
        Specification<Assurance> spec = Specification.allOf(AssuranceSpecifications.vehicule(vehiculeId));
        if (principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL) {
            if (principal.getProvinceId() == null) return List.of();
            spec = spec.and(AssuranceSpecifications.province(principal.getProvinceId()));
        }
        return assuranceRepository.findAll(spec).stream().map(AssuranceMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AssuranceResponse parId(Long id, UserPrincipal principal) {
        Assurance a = assuranceRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Assurance introuvable : " + id, HttpStatus.NOT_FOUND));
        verifierPortee(a.getVehicule(), principal, "Assurance introuvable : " + id);
        return AssuranceMapper.toResponse(a);
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
