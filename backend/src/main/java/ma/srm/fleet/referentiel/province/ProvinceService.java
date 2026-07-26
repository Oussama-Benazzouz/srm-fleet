package ma.srm.fleet.referentiel.province;

import java.util.List;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.referentiel.province.dto.ProvinceRequest;
import ma.srm.fleet.referentiel.province.dto.ProvinceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service metier de gestion du referentiel des provinces (RF-28).
 *
 * Plus simple que UtilisateurService : pas de hachage, pas de resolution
 * de relation complexe, une seule regle metier (unicite du libelle).
 * Aucune autorisation geree ici non plus : comme pour les autres modules,
 * RG14/RF-28 sont appliques en amont, au niveau du controleur
 * (@PreAuthorize), pas dans le service.
 */
@Service
@Transactional
public class ProvinceService {

    private final ProvinceRepository repository;

    public ProvinceService(ProvinceRepository repository) {
        this.repository = repository;
    }

    /**
     * Liste toutes les provinces.
     *
     * Conversion manuelle en DTO via un mapping inline (pas de methode
     * privee dediee toResponse(), contrairement a UtilisateurService) :
     * justifie ici par la simplicite de l'entite (deux champs seulement),
     * la factorisation n'apporterait pas de gain de lisibilite reel.
     */
    @Transactional(readOnly = true)
    public List<ProvinceResponse> lister() {
        return repository.findAll().stream()
            .map(p -> new ProvinceResponse(p.getId(), p.getLibelle())).toList();
    }

    /**
     * Cree une nouvelle province.
     *
     * Un seul controle metier : unicite du libelle (evite les doublons
     * dans le referentiel geographique, cf. ProvinceRepository.
     * existsByLibelle).
     *
     * @param req les donnees de la province a creer
     * @return la province creee, au format DTO
     * @throws BusinessException (409) si une province porte deja ce libelle
     */
    public ProvinceResponse creer(ProvinceRequest req) {
        if (repository.existsByLibelle(req.libelle())) {
            throw new BusinessException("Cette province existe déjà", HttpStatus.CONFLICT);
        }
        Province p = new Province();
        p.setLibelle(req.libelle());
        p = repository.save(p);
        return new ProvinceResponse(p.getId(), p.getLibelle());
    }

    /**
     * Modifie le libelle d'une province existante.
     *
     * Le controle d'unicite n'est declenche que si le libelle a reellement
     * change (!p.getLibelle().equals(req.libelle())) : sans cette
     * condition, modifier une province sans toucher a son propre libelle
     * declencherait systématiquement une fausse erreur de doublon
     * (puisque son libelle "existe deja" — le sien), exactement comme
     * pour UtilisateurService.modifier sur le login/email.
     *
     * @param id  l'identifiant de la province a modifier
     * @param req les nouvelles donnees
     * @return la province modifiee, au format DTO
     * @throws BusinessException (404) si la province n'existe pas
     * @throws BusinessException (409) si un AUTRE province porte deja ce
     *         nouveau libelle
     */
    public ProvinceResponse modifier(Long id, ProvinceRequest req) {
        Province p = repository.findById(id)
            .orElseThrow(() -> new BusinessException("Province introuvable : " + id, HttpStatus.NOT_FOUND));
        if (!p.getLibelle().equals(req.libelle()) && repository.existsByLibelle(req.libelle())) {
            throw new BusinessException("Cette province existe déjà", HttpStatus.CONFLICT);
        }
        p.setLibelle(req.libelle());
        return new ProvinceResponse(p.getId(), p.getLibelle());
    }
}
