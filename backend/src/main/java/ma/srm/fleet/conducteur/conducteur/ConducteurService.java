package ma.srm.fleet.conducteur.conducteur;

import java.util.List;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.conducteur.conducteur.dto.ConducteurFilter;
import ma.srm.fleet.conducteur.conducteur.dto.ConducteurRequest;
import ma.srm.fleet.conducteur.conducteur.dto.ConducteurResponse;
import ma.srm.fleet.conducteur.permis.Permis;
import ma.srm.fleet.conducteur.permis.PermisRepository;
import ma.srm.fleet.conducteur.permis.dto.PermisRequest;
import ma.srm.fleet.conducteur.permis.dto.PermisResponse;
import ma.srm.fleet.referentiel.departement.Departement;
import ma.srm.fleet.referentiel.departement.DepartementRepository;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.province.ProvinceRepository;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConducteurService {

    private final ConducteurRepository conducteurRepository;
    private final PermisRepository permisRepository;
    private final DepartementRepository departementRepository;
    private final ProvinceRepository provinceRepository;

    public ConducteurService(ConducteurRepository conducteurRepository,
                             PermisRepository permisRepository,
                             DepartementRepository departementRepository,
                             ProvinceRepository provinceRepository) {
        this.conducteurRepository = conducteurRepository;
        this.permisRepository = permisRepository;
        this.departementRepository = departementRepository;
        this.provinceRepository = provinceRepository;
    }

    @Transactional(readOnly = true)
    public List<ConducteurResponse> lister(ConducteurFilter filtre, UserPrincipal principal) {
        Specification<Conducteur> spec = Specification.allOf(
            ConducteurSpecifications.recherche(filtre.recherche()),
            ConducteurSpecifications.province(filtre.provinceId()),
            ConducteurSpecifications.departement(filtre.departementId()),
            porteeProvinciale(principal));
        return conducteurRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ConducteurResponse parId(Long id, UserPrincipal principal) {
        Conducteur c = charger(id);
        verifierPortee(c, principal);
        return toResponse(c);
    }

    public ConducteurResponse creer(ConducteurRequest req) {
        if (conducteurRepository.existsByMatricule(req.matricule())) {
            throw new BusinessException("Ce matricule est déjà utilisé", HttpStatus.CONFLICT);
        }
        Conducteur c = new Conducteur();
        appliquer(c, req);
        return toResponse(conducteurRepository.save(c));
    }

    public ConducteurResponse modifier(Long id, ConducteurRequest req) {
        Conducteur c = charger(id);
        if (!c.getMatricule().equals(req.matricule()) && conducteurRepository.existsByMatricule(req.matricule())) {
            throw new BusinessException("Ce matricule est déjà utilisé", HttpStatus.CONFLICT);
        }
        appliquer(c, req);
        return toResponse(c);
    }

    public void supprimer(Long id) {
        Conducteur c = charger(id);
        conducteurRepository.delete(c);   // cascade ALL + orphanRemoval → supprime aussi les permis
    }

    public PermisResponse ajouterPermis(Long conducteurId, PermisRequest req) {
        Conducteur c = charger(conducteurId);
        Permis p = new Permis();
        p.setTypePermis(req.typePermis());
        p.setDateFinValidite(req.dateFinValidite());
        p.setConducteur(c);
        return toPermis(permisRepository.save(p));
    }

    public void supprimerPermis(Long conducteurId, Long permisId) {
        Permis p = permisRepository.findById(permisId)
            .orElseThrow(() -> new BusinessException("Permis introuvable", HttpStatus.NOT_FOUND));
        if (!p.getConducteur().getId().equals(conducteurId)) {
            throw new BusinessException("Ce permis n'appartient pas à ce conducteur", HttpStatus.BAD_REQUEST);
        }
        permisRepository.delete(p);
    }

    // --- Portée provinciale (RG14) : appliquée côté serveur ---

    /** Restreint la liste à la province du Responsable Provincial ; aucune restriction pour l'admin central. */
    private Specification<Conducteur> porteeProvinciale(UserPrincipal principal) {
        return principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL
            ? ConducteurSpecifications.province(principal.getProvinceId())
            : Specification.unrestricted();
    }

    /**
     * Empêche un Responsable Provincial d'accéder à un conducteur hors de sa province.
     * On renvoie 404 (et non 403) pour ne pas révéler l'existence d'un conducteur d'une autre province.
     */
    private void verifierPortee(Conducteur c, UserPrincipal principal) {
        if (principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL
            && !c.getProvince().getId().equals(principal.getProvinceId())) {
            throw new BusinessException("Conducteur introuvable : " + c.getId(), HttpStatus.NOT_FOUND);
        }
    }

    // --- Mapping / résolution des références (mapping inline, cohérent avec UtilisateurService) ---

    private void appliquer(Conducteur c, ConducteurRequest req) {
        c.setMatricule(req.matricule());
        c.setNom(req.nom());
        c.setPrenom(req.prenom());
        c.setFonction(req.fonction());
        c.setGroupeSanguin(req.groupeSanguin());
        c.setProvince(resoudreProvince(req.provinceId()));
        c.setDepartement(resoudreDepartement(req.departementId()));
    }

    private Province resoudreProvince(Long id) {
        return provinceRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Province introuvable", HttpStatus.BAD_REQUEST));
    }

    private Departement resoudreDepartement(Long id) {
        return departementRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Département introuvable", HttpStatus.BAD_REQUEST));
    }

    private Conducteur charger(Long id) {
        return conducteurRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Conducteur introuvable : " + id, HttpStatus.NOT_FOUND));
    }

    private ConducteurResponse toResponse(Conducteur c) {
        Province p = c.getProvince();
        Departement d = c.getDepartement();
        List<PermisResponse> permis = c.getPermis() == null ? List.of()
            : c.getPermis().stream().map(this::toPermis).toList();
        return new ConducteurResponse(
            c.getId(), c.getMatricule(), c.getNom(), c.getPrenom(), c.getFonction(), c.getGroupeSanguin(),
            p != null ? p.getId() : null, p != null ? p.getLibelle() : null,
            d != null ? d.getId() : null, d != null ? d.getLibelle() : null,
            permis);
    }

    private PermisResponse toPermis(Permis p) {
        return new PermisResponse(p.getId(), p.getTypePermis(), p.getDateFinValidite());
    }
}
