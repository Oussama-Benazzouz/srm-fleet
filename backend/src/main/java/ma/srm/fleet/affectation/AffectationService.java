package ma.srm.fleet.affectation;

import java.time.LocalDate;
import java.util.List;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.affectation.dto.AffectationFilter;
import ma.srm.fleet.affectation.dto.AffectationRequest;
import ma.srm.fleet.affectation.dto.AffectationResponse;
import ma.srm.fleet.affectation.dto.ClotureRequest;
import ma.srm.fleet.common.exception.BusinessException;
import org.springframework.data.jpa.domain.Specification;
import ma.srm.fleet.conducteur.conducteur.Conducteur;
import ma.srm.fleet.conducteur.conducteur.ConducteurRepository;
import ma.srm.fleet.referentiel.vehicule.StatutVehicule;
import ma.srm.fleet.referentiel.vehicule.Vehicule;
import ma.srm.fleet.referentiel.vehicule.VehiculeRepository;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

@Service
@Transactional
public class AffectationService {

    private final AffectationRepository affectationRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ConducteurRepository conducteurRepository;

    public AffectationService(AffectationRepository affectationRepository,
                              VehiculeRepository vehiculeRepository,
                              ConducteurRepository conducteurRepository) {
        this.affectationRepository = affectationRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.conducteurRepository = conducteurRepository;
    }

    public AffectationResponse creer(AffectationRequest req, UserPrincipal principal) {
        Vehicule v = vehiculeRepository.findById(req.vehiculeId())
            .orElseThrow(() -> new BusinessException("Véhicule introuvable", HttpStatus.BAD_REQUEST));
        Conducteur c = conducteurRepository.findById(req.conducteurId())
            .orElseThrow(() -> new BusinessException("Conducteur introuvable", HttpStatus.BAD_REQUEST));

        // Portée provinciale : un responsable ne peut affecter que dans SA province
        if (v.getProvince() == null || c.getProvince() == null
            || !v.getProvince().getId().equals(c.getProvince().getId())) {
            throw new BusinessException(
                "Le véhicule et le conducteur doivent appartenir à la même province", HttpStatus.BAD_REQUEST);
        }
        AffectationScope.verifierCreation(v, principal);

        // RG3 (contrôle applicatif, doublé par l'index partiel en base)
        if (affectationRepository.existsByVehiculeIdAndStatut(v.getId(), StatutAffectation.ACTIVE)) {
            throw new BusinessException("Ce véhicule a déjà une affectation active (RG3)", HttpStatus.CONFLICT);
        }
        if (v.getStatut() != StatutVehicule.DISPONIBLE) {
            throw new BusinessException("Le véhicule n'est pas disponible", HttpStatus.CONFLICT);
        }

        Affectation a = new Affectation();
        a.setVehicule(v);
        a.setConducteur(c);
        a.setDateDebut(req.dateDebut() != null ? req.dateDebut() : LocalDate.now());
        a.setMotif(req.motif());
        a.setStatut(StatutAffectation.ACTIVE);
        try {
            a = affectationRepository.saveAndFlush(a);
        } catch (DataIntegrityViolationException e) {
            // Course concurrente : l'index partiel RG3 a tranché à notre place.
            throw new BusinessException("Ce véhicule a déjà une affectation active (RG3)", HttpStatus.CONFLICT);
        }

        v.setStatut(StatutVehicule.AFFECTE);   // synchro statut véhicule

        return AffectationMapper.toResponse(a);
    }

    public AffectationResponse cloturer(Long id, ClotureRequest req, UserPrincipal principal) {
        Affectation a = charger(id);
        AffectationScope.verifier(a, principal);

        if (a.getStatut() != StatutAffectation.ACTIVE) {
            throw new BusinessException("Cette affectation est déjà clôturée", HttpStatus.CONFLICT);
        }
        LocalDate fin = req.dateFin() != null ? req.dateFin() : LocalDate.now();
        if (fin.isBefore(a.getDateDebut())) {
            throw new BusinessException("La date de fin doit être postérieure à la date de début (RF-08)",
                HttpStatus.BAD_REQUEST);
        }

        a.setStatut(StatutAffectation.CLOTUREE);
        a.setDateFin(fin);
        if (a.getVehicule().getStatut() == StatutVehicule.AFFECTE) {
            a.getVehicule().setStatut(StatutVehicule.DISPONIBLE);   // le véhicule redevient disponible
        }

        return AffectationMapper.toResponse(a);
    }

    /**
     * Historique des affectations d'un véhicule (RF-10), plus récent d'abord.
     * Portée provinciale vérifiée une seule fois sur le véhicule racine : un responsable
     * hors province reçoit 404 (existence masquée) plutôt qu'un contrôle ligne par ligne.
     */
    @Transactional(readOnly = true)
    public List<AffectationResponse> historiqueVehicule(Long vehiculeId, UserPrincipal principal) {
        Vehicule v = vehiculeRepository.findById(vehiculeId)
            .orElseThrow(() -> new BusinessException("Véhicule introuvable : " + vehiculeId, HttpStatus.NOT_FOUND));
        AffectationScope.verifierProvince(v.getProvince(), principal);
        return affectationRepository.findByVehiculeIdOrderByDateDebutDesc(vehiculeId).stream()
            .map(AffectationMapper::toResponse).toList();
    }

    /**
     * Historique des affectations d'un conducteur (RF-10), plus récent d'abord.
     * La portée est vérifiée sur la province du CONDUCTEUR (et non des véhicules affectés),
     * afin qu'un responsable voie l'intégralité de l'historique d'un conducteur de sa province.
     */
    @Transactional(readOnly = true)
    public List<AffectationResponse> historiqueConducteur(Long conducteurId, UserPrincipal principal) {
        Conducteur c = conducteurRepository.findById(conducteurId)
            .orElseThrow(() -> new BusinessException("Conducteur introuvable : " + conducteurId, HttpStatus.NOT_FOUND));
        AffectationScope.verifierProvince(c.getProvince(), principal);
        return affectationRepository.findByConducteurIdOrderByDateDebutDesc(conducteurId).stream()
            .map(AffectationMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AffectationResponse> lister(AffectationFilter filtre, UserPrincipal principal) {
        Specification<Affectation> spec = Specification.allOf(
            AffectationSpecifications.statut(filtre.statut()),
            AffectationSpecifications.vehicule(filtre.vehiculeId()),
            AffectationSpecifications.conducteur(filtre.conducteurId()));

        if (principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL) {
            if (principal.getProvinceId() == null) {
                return List.of();   // compte provincial sans province rattachée : périmètre vide
            }
            spec = spec.and(AffectationSpecifications.province(principal.getProvinceId()));
        }
        return affectationRepository.findAll(spec).stream().map(AffectationMapper::toResponse).toList();
    }

    private Affectation charger(Long id) {
        return affectationRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Affectation introuvable : " + id, HttpStatus.NOT_FOUND));
    }
}
