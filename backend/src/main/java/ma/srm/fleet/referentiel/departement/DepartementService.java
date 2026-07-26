package ma.srm.fleet.referentiel.departement;

import java.util.List;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.referentiel.departement.dto.DepartementRequest;
import ma.srm.fleet.referentiel.departement.dto.DepartementResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service metier de gestion du referentiel des departements (RF-28).
 *
 * Duplique exactement le patron de ProvinceService : meme simplicite
 * (deux champs), meme absence de resolution de relation, meme regle
 * unique (unicite du libelle a la creation).
 */
@Service
@Transactional
public class DepartementService {

    private final DepartementRepository repository;

    public DepartementService(DepartementRepository repository) {
        this.repository = repository;
    }

    /**
     * Liste tous les departements.
     */
    @Transactional(readOnly = true)
    public List<DepartementResponse> lister() {
        return repository.findAll().stream()
            .map(d -> new DepartementResponse(d.getId(), d.getLibelle())).toList();
    }

    /**
     * Cree un nouveau departement.
     *
     * @param req les donnees du departement a creer
     * @return le departement cree, au format DTO
     * @throws BusinessException (409) si un departement porte deja ce libelle
     */
    public DepartementResponse creer(DepartementRequest req) {
        if (repository.existsByLibelle(req.libelle())) {
            throw new BusinessException("Ce département existe déjà", HttpStatus.CONFLICT);
        }
        Departement d = new Departement();
        d.setLibelle(req.libelle());
        d = repository.save(d);
        return new DepartementResponse(d.getId(), d.getLibelle());
    }

    /**
     * Modifie le libelle d'un departement existant.
     *
     * @param id  l'identifiant du departement a modifier
     * @param req les nouvelles donnees
     * @return le departement modifie, au format DTO
     * @throws BusinessException (404) si le departement n'existe pas
     * @throws BusinessException (409) si un AUTRE departement porte deja
     *         ce nouveau libelle
     */
    public DepartementResponse modifier(Long id, DepartementRequest req) {
        Departement d = repository.findById(id)
            .orElseThrow(() -> new BusinessException("Département introuvable : " + id, HttpStatus.NOT_FOUND));
        if (!d.getLibelle().equals(req.libelle()) && repository.existsByLibelle(req.libelle())) {
            throw new BusinessException("Ce département existe déjà", HttpStatus.CONFLICT);
        }
        d.setLibelle(req.libelle());
        return new DepartementResponse(d.getId(), d.getLibelle());
    }
}
