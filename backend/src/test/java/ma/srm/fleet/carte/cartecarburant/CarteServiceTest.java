package ma.srm.fleet.carte.cartecarburant;

import java.math.BigDecimal;
import java.util.Optional;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.affectation.Affectation;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.referentiel.fournisseur.Fournisseur;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.vehicule.Vehicule;
import ma.srm.fleet.carte.demande.DemandeCarteCarburant;
import ma.srm.fleet.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarteServiceTest {

    @Mock CarteCarburantRepository carteRepository;
    @InjectMocks CarteService service;

    private UserPrincipal principal(RoleUtilisateur role, Long provinceId) {
        UserPrincipal p = mock(UserPrincipal.class);
        lenient().when(p.getRole()).thenReturn(role);
        lenient().when(p.getProvinceId()).thenReturn(provinceId);
        return p;
    }

    private CarteCarburant carte(Long id, Long provinceId, StatutCarte statut) {
        Province p = new Province(); p.setId(provinceId); p.setLibelle("P" + provinceId);
        Vehicule v = new Vehicule(); v.setId(9L); v.setImmatriculation("X-9"); v.setProvince(p);
        Affectation a = new Affectation(); a.setId(1L); a.setVehicule(v);
        Fournisseur f = new Fournisseur(); f.setId(4L); f.setRaisonSociale("LocAuto");
        DemandeCarteCarburant d = new DemandeCarteCarburant(); d.setId(2L);
        CarteCarburant c = new CarteCarburant();
        c.setId(id); c.setNumeroCarte("C" + id); c.setPlafondMontant(new BigDecimal("3000"));
        c.setStatut(statut); c.setAffectation(a); c.setFournisseur(f); c.setDemandeOrigine(d);
        return c;
    }

    @Test
    void cloturer_ok() {
        CarteCarburant c = carte(1L, 1L, StatutCarte.ACTIVE);
        when(carteRepository.findById(1L)).thenReturn(Optional.of(c));
        assertThat(service.cloturer(1L).statut()).isEqualTo(StatutCarte.CLOTUREE);
    }

    @Test
    void cloturer_dejaCloturee_leve409() {
        when(carteRepository.findById(1L)).thenReturn(Optional.of(carte(1L, 1L, StatutCarte.CLOTUREE)));
        assertThatThrownBy(() -> service.cloturer(1L))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void parId_horsProvince_leve404() {
        when(carteRepository.findById(1L)).thenReturn(Optional.of(carte(1L, 1L, StatutCarte.ACTIVE)));
        assertThatThrownBy(() -> service.parId(1L, principal(RoleUtilisateur.RESPONSABLE_PROVINCIAL, 2L)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
