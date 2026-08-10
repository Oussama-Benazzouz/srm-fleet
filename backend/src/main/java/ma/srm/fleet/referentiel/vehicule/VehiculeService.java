package ma.srm.fleet.referentiel.vehicule;

import java.util.List;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.referentiel.departement.Departement;
import ma.srm.fleet.referentiel.departement.DepartementRepository;
import ma.srm.fleet.referentiel.fournisseur.Fournisseur;
import ma.srm.fleet.referentiel.fournisseur.FournisseurRepository;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.province.ProvinceRepository;
import ma.srm.fleet.referentiel.vehicule.dto.VehiculeRequest;
import ma.srm.fleet.referentiel.vehicule.dto.VehiculeResponse;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final ProvinceRepository provinceRepository;
    private final DepartementRepository departementRepository;
    private final FournisseurRepository fournisseurRepository;

    public VehiculeService(VehiculeRepository vehiculeRepository,
                           ProvinceRepository provinceRepository,
                           DepartementRepository departementRepository,
                           FournisseurRepository fournisseurRepository) {
        this.vehiculeRepository = vehiculeRepository;
        this.provinceRepository = provinceRepository;
        this.departementRepository = departementRepository;
        this.fournisseurRepository = fournisseurRepository;
    }

    @Transactional(readOnly = true)
    public List<VehiculeResponse> rechercher(UserPrincipal user, String immatriculation,
                                             String type, StatutVehicule statut) {
        Specification<Vehicule> spec = VehiculeSpecifications.immatriculationContient(immatriculation)
            .and(VehiculeSpecifications.typeContient(type))
            .and(VehiculeSpecifications.statutEgal(statut));

        if (user.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL) {
            spec = spec.and(VehiculeSpecifications.provinceEgale(user.getProvinceId()));
        }
        return vehiculeRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VehiculeResponse parId(UserPrincipal user, Long id) {
        Vehicule v = charger(id);
        verifierPortee(v, user);
        return toResponse(v);
    }

    /**
     * Empêche un Responsable Provincial d'accéder à un véhicule hors de sa province (RG14).
     * On renvoie 404 (et non 403) pour ne pas révéler l'existence d'un véhicule d'une autre province,
     * cohérent avec ConducteurService.verifierPortee.
     */
    private void verifierPortee(Vehicule v, UserPrincipal user) {
        if (user.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL
            && !v.getProvince().getId().equals(user.getProvinceId())) {
            throw new BusinessException("Véhicule introuvable : " + v.getId(), HttpStatus.NOT_FOUND);
        }
    }

    public VehiculeResponse creer(VehiculeRequest req) {
        if (vehiculeRepository.existsByImmatriculation(req.immatriculation())) {
            throw new BusinessException("Cette immatriculation existe déjà", HttpStatus.CONFLICT);
        }
        Vehicule v = new Vehicule();
        appliquer(v, req);
        return toResponse(vehiculeRepository.save(v));
    }

    public VehiculeResponse modifier(Long id, VehiculeRequest req) {
        Vehicule v = charger(id);
        if (!v.getImmatriculation().equals(req.immatriculation())
            && vehiculeRepository.existsByImmatriculation(req.immatriculation())) {
            throw new BusinessException("Cette immatriculation existe déjà", HttpStatus.CONFLICT);
        }
        appliquer(v, req);
        return toResponse(v);
    }

    private void appliquer(Vehicule v, VehiculeRequest req) {
        if (req.modeAcquisition() == ModeAcquisition.LOCATION && req.fournisseurId() == null) {
            throw new BusinessException("Un véhicule en location doit avoir un fournisseur (RG2)", HttpStatus.BAD_REQUEST);
        }
        v.setImmatriculation(req.immatriculation());
        v.setTypeVehicule(req.typeVehicule());
        v.setModeAcquisition(req.modeAcquisition());
        v.setDateMiseEnService(req.dateMiseEnService());
        v.setStatut(req.statut());
        v.setNumeroContrat(req.numeroContrat());
        v.setDateDebutContrat(req.dateDebutContrat());
        v.setDateFinContrat(req.dateFinContrat());
        v.setLoyerMensuel(req.loyerMensuel());
        v.setPlafondKm(req.plafondKm());
        v.setProvince(resoudreProvince(req.provinceId()));
        v.setDepartement(resoudreDepartement(req.departementId()));
        v.setFournisseur(resoudreFournisseur(req.fournisseurId()));
    }

    private Province resoudreProvince(Long id) {
        return provinceRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Province introuvable", HttpStatus.BAD_REQUEST));
    }
    private Departement resoudreDepartement(Long id) {
        if (id == null) return null;
        return departementRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Département introuvable", HttpStatus.BAD_REQUEST));
    }
    private Fournisseur resoudreFournisseur(Long id) {
        if (id == null) return null;
        return fournisseurRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Fournisseur introuvable", HttpStatus.BAD_REQUEST));
    }
    private Vehicule charger(Long id) {
        return vehiculeRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Véhicule introuvable : " + id, HttpStatus.NOT_FOUND));
    }

    private VehiculeResponse toResponse(Vehicule v) {
        Province p = v.getProvince();
        Departement d = v.getDepartement();
        Fournisseur f = v.getFournisseur();
        return new VehiculeResponse(
            v.getId(), v.getImmatriculation(), v.getTypeVehicule(), v.getModeAcquisition(),
            v.getDateMiseEnService(), v.getStatut(), v.getNumeroContrat(),
            v.getDateDebutContrat(), v.getDateFinContrat(), v.getLoyerMensuel(), v.getPlafondKm(),
            p != null ? p.getId() : null, p != null ? p.getLibelle() : null,
            d != null ? d.getId() : null, d != null ? d.getLibelle() : null,
            f != null ? f.getId() : null, f != null ? f.getRaisonSociale() : null);
    }
}
