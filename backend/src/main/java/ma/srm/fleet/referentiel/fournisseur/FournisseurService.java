package ma.srm.fleet.referentiel.fournisseur;

import java.util.List;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.referentiel.fournisseur.dto.FournisseurRequest;
import ma.srm.fleet.referentiel.fournisseur.dto.FournisseurResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FournisseurService {

    private final FournisseurRepository repository;

    public FournisseurService(FournisseurRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<FournisseurResponse> lister() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public FournisseurResponse creer(FournisseurRequest req) {
        Fournisseur f = new Fournisseur();
        appliquer(f, req);
        return toResponse(repository.save(f));
    }

    public FournisseurResponse modifier(Long id, FournisseurRequest req) {
        Fournisseur f = repository.findById(id)
            .orElseThrow(() -> new BusinessException("Fournisseur introuvable : " + id, HttpStatus.NOT_FOUND));
        appliquer(f, req);
        return toResponse(f);
    }

    private void appliquer(Fournisseur f, FournisseurRequest req) {
        f.setRaisonSociale(req.raisonSociale());
        f.setType(req.type());
        f.setContact(req.contact());
        f.setAdresseFacturation(req.adresseFacturation());
    }

    private FournisseurResponse toResponse(Fournisseur f) {
        return new FournisseurResponse(f.getId(), f.getRaisonSociale(), f.getType(),
            f.getContact(), f.getAdresseFacturation());
    }
}
