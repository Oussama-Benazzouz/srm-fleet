package ma.srm.fleet.conformite.visitetechnique;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.affectation.Affectation;
import ma.srm.fleet.affectation.AffectationRepository;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.conducteur.conducteur.Conducteur;
import ma.srm.fleet.conformite.visitetechnique.dto.VisiteRequest;
import ma.srm.fleet.conformite.visitetechnique.dto.VisiteResponse;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.vehicule.Vehicule;
import ma.srm.fleet.referentiel.vehicule.VehiculeRepository;
import ma.srm.fleet.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisiteTechniqueServiceTest {

    @Mock VisiteTechniqueRepository visiteRepository;
    @Mock VehiculeRepository vehiculeRepository;
    @Mock AffectationRepository affectationRepository;
    @InjectMocks VisiteTechniqueService service;

    private UserPrincipal principal(RoleUtilisateur role, Long provinceId) {
        UserPrincipal p = mock(UserPrincipal.class);
        lenient().when(p.getRole()).thenReturn(role);
        lenient().when(p.getProvinceId()).thenReturn(provinceId);
        return p;
    }

    private Vehicule vehicule(Long provinceId) {
        Province p = new Province(); p.setId(provinceId); p.setLibelle("P" + provinceId);
        Vehicule v = new Vehicule(); v.setId(9L); v.setImmatriculation("X-9"); v.setProvince(p);
        return v;
    }

    private Conducteur conducteur() {
        Conducteur c = new Conducteur();
        c.setId(5L); c.setNom("Alaoui"); c.setPrenom("Sara"); c.setMatricule("M-5");
        return c;
    }

    private Affectation affectation(Vehicule v, Conducteur c) {
        Affectation a = new Affectation(); a.setId(3L); a.setVehicule(v); a.setConducteur(c);
        return a;
    }

    @Test
    void creer_deriveConducteurAccompagnateur_depuisAffectation() {
        Vehicule v = vehicule(1L);
        when(vehiculeRepository.findById(9L)).thenReturn(Optional.of(v));
        when(affectationRepository.findCouvrantDate(eq(9L), any(LocalDate.class)))
            .thenReturn(List.of(affectation(v, conducteur())));
        when(visiteRepository.save(any(VisiteTechnique.class))).thenAnswer(i -> i.getArgument(0));

        VisiteResponse res = service.creer(
            new VisiteRequest(9L, LocalDate.parse("2026-06-01"), LocalDate.parse("2027-06-01"), "Norisko"),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null));

        assertThat(res.conducteurId()).isEqualTo(5L);
        assertThat(res.matriculeConducteur()).isEqualTo("M-5");
        assertThat(res.conducteurNomComplet()).isEqualTo("Sara Alaoui");
    }

    @Test
    void creer_sansAffectationCouvrante_leve409() {
        when(vehiculeRepository.findById(9L)).thenReturn(Optional.of(vehicule(1L)));
        when(affectationRepository.findCouvrantDate(eq(9L), any(LocalDate.class))).thenReturn(List.of());
        assertThatThrownBy(() -> service.creer(
            new VisiteRequest(9L, LocalDate.parse("2026-06-01"), LocalDate.parse("2027-06-01"), "Norisko"),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void creer_echeanceAvantVisite_leve400() {
        when(vehiculeRepository.findById(9L)).thenReturn(Optional.of(vehicule(1L)));
        assertThatThrownBy(() -> service.creer(
            new VisiteRequest(9L, LocalDate.parse("2026-06-01"), LocalDate.parse("2026-05-01"), "Norisko"),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void creer_provincialHorsProvince_leve404() {
        when(vehiculeRepository.findById(9L)).thenReturn(Optional.of(vehicule(1L)));   // province 1
        assertThatThrownBy(() -> service.creer(
            new VisiteRequest(9L, LocalDate.parse("2026-06-01"), LocalDate.parse("2027-06-01"), "Norisko"),
            principal(RoleUtilisateur.RESPONSABLE_PROVINCIAL, 2L)))                    // provincial de 2
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
