package ma.srm.fleet.conducteur.conducteur;

import java.util.List;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.conducteur.conducteur.dto.ConducteurRequest;
import ma.srm.fleet.conducteur.conducteur.dto.ConducteurResponse;
import ma.srm.fleet.conducteur.permis.Permis;
import ma.srm.fleet.conducteur.permis.PermisRepository;
import ma.srm.fleet.conducteur.permis.dto.PermisRequest;
import ma.srm.fleet.conducteur.permis.dto.PermisResponse;
import ma.srm.fleet.referentiel.departement.Departement;
import ma.srm.fleet.referentiel.departement.DepartementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConducteurService {

    private final ConducteurRepository conducteurRepository;
    private final PermisRepository permisRepository;
    private final DepartementRepository departementRepository;

    public ConducteurService(ConducteurRepository conducteurRepository,
                             PermisRepository permisRepository,
                             DepartementRepository departementRepository) {
        this.conducteurRepository = conducteurRepository;
        this.permisRepository = permisRepository;
        this.departementRepository = departementRepository;
    }

    @Transactional(readOnly = true)
    public List<ConducteurResponse> lister(String recherche) {
        List<Conducteur> conducteurs = (recherche == null || recherche.isBlank())
                ? conducteurRepository.findAll()
                : conducteurRepository.rechercher(recherche);
        return conducteurs.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ConducteurResponse parId(Long id) {
        return toResponse(charger(id));
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

    private void appliquer(Conducteur c, ConducteurRequest req) {
        c.setMatricule(req.matricule());
        c.setNom(req.nom());
        c.setPrenom(req.prenom());
        c.setFonction(req.fonction());
        c.setGroupeSanguin(req.groupeSanguin());
        c.setDepartement(resoudreDepartement(req.departementId()));
    }

    private Departement resoudreDepartement(Long id) {
        if (id == null) return null;
        return departementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Département introuvable", HttpStatus.BAD_REQUEST));
    }

    private Conducteur charger(Long id) {
        return conducteurRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Conducteur introuvable : " + id, HttpStatus.NOT_FOUND));
    }

    private ConducteurResponse toResponse(Conducteur c) {
        Departement d = c.getDepartement();
        List<PermisResponse> permis = c.getPermis() == null ? List.of()
                : c.getPermis().stream().map(this::toPermis).toList();
        return new ConducteurResponse(
                c.getId(), c.getMatricule(), c.getNom(), c.getPrenom(), c.getFonction(), c.getGroupeSanguin(),
                d != null ? d.getId() : null, d != null ? d.getLibelle() : null, permis);
    }

    private PermisResponse toPermis(Permis p) {
        return new PermisResponse(p.getId(), p.getTypePermis(), p.getDateFinValidite());
    }
}