package ma.srm.fleet.conformite.document;

import java.time.LocalDateTime;
import java.util.List;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.conformite.document.dto.DocumentResponse;
import ma.srm.fleet.conformite.sinistre.Sinistre;
import ma.srm.fleet.conformite.sinistre.SinistreRepository;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final SinistreRepository sinistreRepository;
    private final DocumentStorageService storage;

    public DocumentService(DocumentRepository documentRepository,
                           SinistreRepository sinistreRepository,
                           DocumentStorageService storage) {
        this.documentRepository = documentRepository;
        this.sinistreRepository = sinistreRepository;
        this.storage = storage;
    }

    /** RF-24 : attacher une pièce jointe à un sinistre (accessible même après clôture). */
    public DocumentResponse televerser(Long sinistreId, MultipartFile fichier, UserPrincipal principal) {
        Sinistre sinistre = chargerSinistre(sinistreId, principal);
        String cheminRelatif = storage.stocker(sinistreId, fichier);
        String original = StringUtils.cleanPath(
            fichier.getOriginalFilename() != null ? fichier.getOriginalFilename() : "document");

        Document d = new Document();
        d.setSinistre(sinistre);
        d.setNomFichier(original);
        d.setTypeDocument(fichier.getContentType());
        d.setCheminStockage(cheminRelatif);
        d.setDateUpload(LocalDateTime.now());
        return toResponse(documentRepository.save(d));
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> lister(Long sinistreId, UserPrincipal principal) {
        chargerSinistre(sinistreId, principal);
        return documentRepository.findBySinistreIdOrderByDateUploadDesc(sinistreId).stream()
            .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DocumentTelechargement telecharger(Long documentId, UserPrincipal principal) {
        Document d = documentRepository.findById(documentId)
            .orElseThrow(() -> new BusinessException("Document introuvable : " + documentId, HttpStatus.NOT_FOUND));
        verifierPortee(d.getSinistre(), principal, "Document introuvable : " + documentId);
        return new DocumentTelechargement(storage.charger(d.getCheminStockage()), d.getNomFichier(), d.getTypeDocument());
    }

    /** Charge un sinistre en masquant son existence à un responsable hors province (404). */
    private Sinistre chargerSinistre(Long sinistreId, UserPrincipal principal) {
        Sinistre s = sinistreRepository.findById(sinistreId)
            .orElseThrow(() -> new BusinessException("Sinistre introuvable : " + sinistreId, HttpStatus.NOT_FOUND));
        verifierPortee(s, principal, "Sinistre introuvable : " + sinistreId);
        return s;
    }

    private void verifierPortee(Sinistre s, UserPrincipal principal, String messageSiHorsPortee) {
        if (principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL) {
            Province prov = s.getVehicule().getProvince();
            if (prov == null || !prov.getId().equals(principal.getProvinceId())) {
                throw new BusinessException(messageSiHorsPortee, HttpStatus.NOT_FOUND);
            }
        }
    }

    private DocumentResponse toResponse(Document d) {
        return new DocumentResponse(
            d.getId(), d.getSinistre().getId(), d.getNomFichier(), d.getTypeDocument(), d.getDateUpload());
    }
}
