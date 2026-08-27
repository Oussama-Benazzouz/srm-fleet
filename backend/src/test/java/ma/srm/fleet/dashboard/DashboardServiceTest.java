package ma.srm.fleet.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.affectation.AffectationRepository;
import ma.srm.fleet.alerte.AlerteService;
import ma.srm.fleet.alerte.StatutLecture;
import ma.srm.fleet.alerte.configuration.TypeSourceAlerte;
import ma.srm.fleet.alerte.dto.AlerteResponse;
import ma.srm.fleet.carte.cartecarburant.CarteCarburantRepository;
import ma.srm.fleet.carte.demande.DemandeCarteCarburantRepository;
import ma.srm.fleet.conducteur.conducteur.ConducteurRepository;
import ma.srm.fleet.conformite.sinistre.SinistreRepository;
import ma.srm.fleet.conformite.sinistre.StatutSinistre;
import ma.srm.fleet.dashboard.dto.DashboardResponse;
import ma.srm.fleet.referentiel.vehicule.ModeAcquisition;
import ma.srm.fleet.referentiel.vehicule.StatutVehicule;
import ma.srm.fleet.referentiel.vehicule.VehiculeRepository;
import ma.srm.fleet.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock VehiculeRepository vehiculeRepository;
    @Mock ConducteurRepository conducteurRepository;
    @Mock AffectationRepository affectationRepository;
    @Mock CarteCarburantRepository carteRepository;
    @Mock DemandeCarteCarburantRepository demandeRepository;
    @Mock SinistreRepository sinistreRepository;
    @Mock AlerteService alerteService;
    @InjectMocks DashboardService service;

    private UserPrincipal principal(RoleUtilisateur role, Long provinceId) {
        UserPrincipal p = mock(UserPrincipal.class);
        lenient().when(p.getRole()).thenReturn(role);
        lenient().when(p.getProvinceId()).thenReturn(provinceId);
        return p;
    }

    private AlerteResponse alerte(TypeSourceAlerte type) {
        return new AlerteResponse(1L, type, 9L, "X-9", 1L, "Agadir",
            LocalDate.now(), 5, null, StatutLecture.NON_LUE, LocalDateTime.now());
    }

    @Test
    void construire_agrege_les_metriques_et_repartitions() {
        when(vehiculeRepository.compterParStatut(null)).thenReturn(List.of(
            new Object[] {StatutVehicule.DISPONIBLE, 3L},
            new Object[] {StatutVehicule.AFFECTE, 2L}));
        when(vehiculeRepository.compterParAcquisition(null)).thenReturn(List.of(
            new Object[] {ModeAcquisition.PROPRIETE, 4L},
            new Object[] {ModeAcquisition.LOCATION, 1L}));
        when(vehiculeRepository.compterParProvince(null)).thenReturn(List.<Object[]>of(
            new Object[] {"Agadir", 5L}));
        when(sinistreRepository.compterParStatut(null)).thenReturn(List.<Object[]>of(
            new Object[] {StatutSinistre.OUVERT, 1L}));
        when(conducteurRepository.compter(null)).thenReturn(6L);
        when(affectationRepository.compterActives(null)).thenReturn(2L);
        when(carteRepository.compterActives(null)).thenReturn(2L);
        when(demandeRepository.compterEnAttente(null)).thenReturn(1L);
        when(alerteService.lister(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(
            alerte(TypeSourceAlerte.ASSURANCE),
            alerte(TypeSourceAlerte.ASSURANCE),
            alerte(TypeSourceAlerte.VISITE_TECHNIQUE)));

        DashboardResponse res = service.construire(principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null));

        assertThat(res.vehicules()).isEqualTo(5);
        assertThat(res.vehiculesDisponibles()).isEqualTo(3);
        assertThat(res.vehiculesAffectes()).isEqualTo(2);
        assertThat(res.vehiculesImmobilises()).isZero();
        assertThat(res.conducteurs()).isEqualTo(6);
        assertThat(res.affectationsActives()).isEqualTo(2);
        assertThat(res.cartesActives()).isEqualTo(2);
        assertThat(res.demandesEnAttente()).isEqualTo(1);
        assertThat(res.alertesActives()).isEqualTo(3);
        assertThat(res.sinistresOuverts()).isEqualTo(1);
        assertThat(res.vehiculesParStatut()).hasSize(4);
        assertThat(res.vehiculesParProvince()).containsExactly(
            new ma.srm.fleet.dashboard.dto.CompteParLibelle("Agadir", 5L));
        assertThat(res.alertesParType()).containsExactly(
            new ma.srm.fleet.dashboard.dto.CompteParLibelle("Assurance", 2L),
            new ma.srm.fleet.dashboard.dto.CompteParLibelle("Visite technique", 1L));
    }

    @Test
    void construire_provincial_sans_province_renvoie_vide() {
        DashboardResponse res = service.construire(principal(RoleUtilisateur.RESPONSABLE_PROVINCIAL, null));

        assertThat(res.vehicules()).isZero();
        assertThat(res.alertesActives()).isZero();
        assertThat(res.vehiculesParStatut()).isEmpty();
    }
}
