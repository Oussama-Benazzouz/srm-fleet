package ma.srm.fleet.conformite.document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import ma.srm.fleet.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stockage des pièces jointes de sinistres sur le système de fichiers, sous un répertoire
 * racine configurable ({@code app.storage.documents}, monté sur un volume Docker dédié afin
 * que les fichiers survivent au cycle de vie du conteneur).
 *
 * <p>Seul le chemin relatif est conservé en base ({@code chemin_stockage_document}) ; le nom
 * de fichier stocké est anonymisé (UUID) pour éviter toute collision et toute traversée de
 * répertoire à partir du nom d'origine fourni par l'utilisateur.</p>
 */
@Service
public class DocumentStorageService {

    private final Path racine;

    public DocumentStorageService(@Value("${app.storage.documents}") String racine) {
        this.racine = Paths.get(racine).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.racine);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible d'initialiser le répertoire de stockage des documents", e);
        }
    }

    /** Écrit le fichier sous {racine}/{sinistreId}/{uuid.ext} et renvoie le chemin relatif à stocker. */
    public String stocker(Long sinistreId, MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new BusinessException("Le fichier est vide", HttpStatus.BAD_REQUEST);
        }
        String original = StringUtils.cleanPath(
            fichier.getOriginalFilename() != null ? fichier.getOriginalFilename() : "document");
        String extension = StringUtils.getFilenameExtension(original);
        String nomStocke = UUID.randomUUID() + (extension != null ? "." + extension : "");
        String cheminRelatif = sinistreId + "/" + nomStocke;

        Path cible = racine.resolve(cheminRelatif).normalize();
        if (!cible.startsWith(racine)) {   // garde anti-traversée de répertoire
            throw new BusinessException("Chemin de stockage invalide", HttpStatus.BAD_REQUEST);
        }
        try {
            Files.createDirectories(cible.getParent());
            Files.copy(fichier.getInputStream(), cible);
        } catch (IOException e) {
            throw new BusinessException("Échec de l'enregistrement du fichier", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return cheminRelatif;
    }

    /** Charge le contenu d'une pièce jointe à partir de son chemin relatif stocké en base. */
    public Resource charger(String cheminRelatif) {
        Path fichier = racine.resolve(cheminRelatif).normalize();
        if (!fichier.startsWith(racine)) {
            throw new BusinessException("Chemin de stockage invalide", HttpStatus.BAD_REQUEST);
        }
        try {
            Resource resource = new UrlResource(fichier.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException("Fichier introuvable sur le stockage", HttpStatus.NOT_FOUND);
            }
            return resource;
        } catch (IOException e) {
            throw new BusinessException("Fichier introuvable sur le stockage", HttpStatus.NOT_FOUND);
        }
    }
}
