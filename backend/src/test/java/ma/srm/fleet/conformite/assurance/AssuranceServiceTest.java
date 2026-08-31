package ma.srm.fleet.conformite.assurance;

import java.time.LocalDate;
import java.util.Optional;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.conformite.assurance.dto.AssuranceRequest;
import ma.srm.fleet.conformite.assurance.dto.AssuranceResponse;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssuranceServiceTest {

    @Mock AssuranceRepository assuranceRepository;
    @Mock VehiculeRepository vehiculeRepository;
    @InjectMocks AssuranceService service;

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

    private Assurance assuranceExistante(Long id) {
        Assurance a = new Assurance();
        a.setId(id);
        a.setVehicule(vehicule(1L));
        a.setNomCompagnie("AXA");
        a.setDateDebut(LocalDate.parse("2026-01-01"));
        a.setDateEcheance(LocalDate.parse("2026-06-30"));
        return a;
    }

    private AssuranceRequest req(String debut, String fin) {
        return new AssuranceRequest(9L, "AXA", LocalDate.parse(debut), LocalDate.parse(fin));
    }

    @Test
    void creer_provincialHorsProvince_leve404() {
        when(vehiculeRepository.findById(9L)).thenReturn(Optional.of(vehicule(1L)));   // province 1
        assertThatThrownBy(() -> service.creer(req("2026-01-01", "2027-01-01"),
            principal(RoleUtilisateur.RESPONSABLE_PROVINCIAL, 2L)))                    // provincial de 2
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void creer_echeanceAvantDebut_leve400() {
        when(vehiculeRepository.findById(9L)).thenReturn(Optional.of(vehicule(1L)));
        assertThatThrownBy(() -> service.creer(req("2027-01-01", "2026-01-01"),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void creer_chevauchement_leve409() {
        when(vehiculeRepository.findById(9L)).thenReturn(Optional.of(vehicule(1L)));
        when(assuranceRepository.existsChevauchement(eq(9L), any(), any(), isNull())).thenReturn(true);
        assertThatThrownBy(() -> service.creer(req("2026-06-01", "2026-06-30"),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void creer_ok() {
        when(vehiculeRepository.findById(9L)).thenReturn(Optional.of(vehicule(1L)));
        when(assuranceRepository.saveAndFlush(any(Assurance.class))).thenAnswer(i -> i.getArgument(0));

        AssuranceResponse res = service.creer(req("2026-01-01", "2027-01-01"),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null));

        assertThat(res.nomCompagnie()).isEqualTo("AXA");
        assertThat(res.vehiculeId()).isEqualTo(9L);
    }

    @Test
    void modifier_sansChevauchement_ok_excluantSoi() {
        when(assuranceRepository.findById(5L)).thenReturn(Optional.of(assuranceExistante(5L)));
        when(vehiculeRepository.findById(9L)).thenReturn(Optional.of(vehicule(1L)));
        when(assuranceRepository.saveAndFlush(any(Assurance.class))).thenAnswer(i -> i.getArgument(0));
        // existsChevauchement(..., excludeId=5) renvoie false par défaut → l'enregistrement ne se chevauche pas avec lui-même

        AssuranceResponse res = service.modifier(5L, req("2026-01-01", "2026-06-30"),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null));

        assertThat(res.dateEcheance()).isEqualTo(LocalDate.parse("2026-06-30"));
    }

    @Test
    void modifier_versPeriodeDunAutre_leve409() {
        when(assuranceRepository.findById(5L)).thenReturn(Optional.of(assuranceExistante(5L)));
        when(vehiculeRepository.findById(9L)).thenReturn(Optional.of(vehicule(1L)));
        when(assuranceRepository.existsChevauchement(eq(9L), any(), any(), eq(5L))).thenReturn(true);

        assertThatThrownBy(() -> service.modifier(5L, req("2026-03-01", "2026-09-30"),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }
}
