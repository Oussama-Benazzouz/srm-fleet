package ma.srm.fleet.conformite.sinistre;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.affectation.Affectation;
import ma.srm.fleet.affectation.AffectationRepository;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.conducteur.conducteur.Conducteur;
import ma.srm.fleet.conformite.sinistre.dto.SinistreRequest;
import ma.srm.fleet.conformite.sinistre.dto.SinistreResponse;
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
class SinistreServiceTest {

    @Mock SinistreRepository sinistreRepository;
    @Mock VehiculeRepository vehiculeRepository;
    @Mock AffectationRepository affectationRepository;
    @InjectMocks SinistreService service;

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

    private Affectation affectation(Vehicule v) {
        Conducteur c = new Conducteur();
        c.setId(5L); c.setNom("Alaoui"); c.setPrenom("Sara"); c.setMatricule("M-5");
        Affectation a = new Affectation(); a.setId(3L); a.setVehicule(v); a.setConducteur(c);
        return a;
    }

    private Sinistre sinistre(StatutSinistre statut, Long provinceId) {
        Sinistre s = new Sinistre();
        s.setId(1L); s.setVehicule(vehicule(provinceId)); s.setStatut(statut);
        s.setDateSinistre(LocalDate.parse("2026-06-01"));
        return s;
    }

    @Test
    void creer_rattacheAffectationCouvrante_RG7() {
        Vehicule v = vehicule(1L);
        when(vehiculeRepository.findById(9L)).thenReturn(Optional.of(v));
        when(affectationRepository.findCouvrantDate(eq(9L), any(LocalDate.class)))
            .thenReturn(List.of(affectation(v)));
        when(sinistreRepository.save(any(Sinistre.class))).thenAnswer(i -> i.getArgument(0));

        SinistreResponse res = service.creer(
            new SinistreRequest(9L, LocalDate.parse("2026-06-01"), "Choc arrière"),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null));

        assertThat(res.statut()).isEqualTo(StatutSinistre.OUVERT);
        assertThat(res.affectationId()).isEqualTo(3L);
        assertThat(res.conducteurNomComplet()).isEqualTo("Sara Alaoui");
    }

    @Test
    void creer_sansAffectation_affectationNull_RG7() {
        Vehicule v = vehicule(1L);
        when(vehiculeRepository.findById(9L)).thenReturn(Optional.of(v));
        when(affectationRepository.findCouvrantDate(eq(9L), any(LocalDate.class))).thenReturn(List.of());
        when(sinistreRepository.save(any(Sinistre.class))).thenAnswer(i -> i.getArgument(0));

        SinistreResponse res = service.creer(
            new SinistreRequest(9L, LocalDate.parse("2026-06-01"), "Vitre brisée au parking"),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null));

        assertThat(res.affectationId()).isNull();
        assertThat(res.conducteurId()).isNull();
        assertThat(res.statut()).isEqualTo(StatutSinistre.OUVERT);
    }

    @Test
    void changerStatut_ouvertVersEnCours_ok() {
        when(sinistreRepository.findById(1L)).thenReturn(Optional.of(sinistre(StatutSinistre.OUVERT, 1L)));
        SinistreResponse res = service.changerStatut(1L, StatutSinistre.EN_COURS,
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null));
        assertThat(res.statut()).isEqualTo(StatutSinistre.EN_COURS);
    }

    @Test
    void changerStatut_sautEtape_ouvertVersCloture_leve409() {
        when(sinistreRepository.findById(1L)).thenReturn(Optional.of(sinistre(StatutSinistre.OUVERT, 1L)));
        assertThatThrownBy(() -> service.changerStatut(1L, StatutSinistre.CLOTURE,
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void changerStatut_provincialHorsProvince_leve404() {
        when(sinistreRepository.findById(1L)).thenReturn(Optional.of(sinistre(StatutSinistre.OUVERT, 1L)));  // province 1
        assertThatThrownBy(() -> service.changerStatut(1L, StatutSinistre.EN_COURS,
            principal(RoleUtilisateur.RESPONSABLE_PROVINCIAL, 2L)))                                          // provincial de 2
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
