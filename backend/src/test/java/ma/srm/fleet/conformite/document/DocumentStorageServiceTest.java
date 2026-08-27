package ma.srm.fleet.conformite.document;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import ma.srm.fleet.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentStorageServiceTest {

    @Test
    void stocker_puisCharger_restitueLeContenu(@TempDir Path tmp) throws Exception {
        DocumentStorageService storage = new DocumentStorageService(tmp.toString());
        MockMultipartFile fichier = new MockMultipartFile(
            "fichier", "constat.pdf", "application/pdf", "contenu-pdf".getBytes(StandardCharsets.UTF_8));

        String chemin = storage.stocker(7L, fichier);

        assertThat(chemin).startsWith("7/").endsWith(".pdf");
        Resource resource = storage.charger(chemin);
        assertThat(resource.exists()).isTrue();
        assertThat(resource.getContentAsString(StandardCharsets.UTF_8)).isEqualTo("contenu-pdf");
    }

    @Test
    void stocker_fichierVide_leve400(@TempDir Path tmp) {
        DocumentStorageService storage = new DocumentStorageService(tmp.toString());
        MockMultipartFile vide = new MockMultipartFile("fichier", "vide.pdf", "application/pdf", new byte[0]);
        assertThatThrownBy(() -> storage.stocker(7L, vide))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void charger_inexistant_leve404(@TempDir Path tmp) {
        DocumentStorageService storage = new DocumentStorageService(tmp.toString());
        assertThatThrownBy(() -> storage.charger("7/inexistant.pdf"))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
