package ma.srm.fleet.conformite.document;

import java.util.List;
import ma.srm.fleet.conformite.document.dto.DocumentResponse;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) { this.service = service; }

    @PostMapping(path = "/sinistres/{sinistreId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR_CENTRAL','RESPONSABLE_PROVINCIAL')")
    public DocumentResponse televerser(@PathVariable Long sinistreId,
                                       @RequestParam("fichier") MultipartFile fichier,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        return service.televerser(sinistreId, fichier, principal);
    }

    @GetMapping("/sinistres/{sinistreId}/documents")
    public List<DocumentResponse> lister(@PathVariable Long sinistreId,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        return service.lister(sinistreId, principal);
    }

    @GetMapping("/documents/{id}/contenu")
    public ResponseEntity<Resource> telecharger(@PathVariable Long id,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        DocumentTelechargement dl = service.telecharger(id, principal);
        MediaType type = dl.typeDocument() != null
            ? MediaType.parseMediaType(dl.typeDocument()) : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
            .contentType(type)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dl.nomFichier() + "\"")
            .body(dl.resource());
    }
}
