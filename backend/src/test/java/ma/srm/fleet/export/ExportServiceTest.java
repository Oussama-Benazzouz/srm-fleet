package ma.srm.fleet.export;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import ma.srm.fleet.affectation.AffectationService;
import ma.srm.fleet.carte.cartecarburant.CarteService;
import ma.srm.fleet.referentiel.vehicule.ModeAcquisition;
import ma.srm.fleet.referentiel.vehicule.StatutVehicule;
import ma.srm.fleet.referentiel.vehicule.VehiculeService;
import ma.srm.fleet.referentiel.vehicule.dto.VehiculeResponse;
import ma.srm.fleet.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock VehiculeService vehiculeService;
    @Mock AffectationService affectationService;
    @Mock CarteService carteService;
    @InjectMocks ExportService service;

    private final UserPrincipal user = mock(UserPrincipal.class);

    private VehiculeResponse vehicule() {
        return new VehiculeResponse(1L, "1234-A-6", "CAMION", ModeAcquisition.PROPRIETE,
            LocalDate.parse("2024-01-01"), StatutVehicule.DISPONIBLE, null, null, null, null, null,
            1L, "Agadir", null, null, null, null);
    }

    @Test
    void vehicules_csv_contient_entetes_et_donnees() {
        when(vehiculeService.rechercher(any(), any(), any(), any())).thenReturn(List.of(vehicule()));

        ExportFichier f = service.vehicules(user, ExportFormat.CSV, null, null, null);

        assertThat(f.contentType()).contains("text/csv");
        assertThat(f.nomFichier()).startsWith("vehicules-").endsWith(".csv");
        String contenu = new String(f.contenu(), StandardCharsets.UTF_8);
        assertThat(contenu).contains("Immatriculation").contains("1234-A-6").contains("Agadir");
    }

    @Test
    void vehicules_pdf_produit_un_binaire_pdf() {
        when(vehiculeService.rechercher(any(), any(), any(), any())).thenReturn(List.of(vehicule()));

        ExportFichier f = service.vehicules(user, ExportFormat.PDF, null, null, null);

        assertThat(f.contentType()).isEqualTo("application/pdf");
        assertThat(f.nomFichier()).endsWith(".pdf");
        assertThat(new String(f.contenu(), 0, 5, StandardCharsets.ISO_8859_1)).startsWith("%PDF-");
    }
}
