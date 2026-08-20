package ma.srm.fleet.carte.demande;

import java.math.BigDecimal;
import java.util.Optional;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.admin.utilisateur.Utilisateur;
import ma.srm.fleet.admin.utilisateur.UtilisateurRepository;
import ma.srm.fleet.affectation.Affectation;
import ma.srm.fleet.affectation.AffectationRepository;
import ma.srm.fleet.carte.cartecarburant.CarteCarburant;
import ma.srm.fleet.carte.cartecarburant.CarteCarburantRepository;
import ma.srm.fleet.carte.cartecarburant.StatutCarte;
import ma.srm.fleet.carte.demande.dto.DemandeRequest;
import ma.srm.fleet.carte.demande.dto.ValiderRequest;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.referentiel.fournisseur.Fournisseur;
import ma.srm.fleet.referentiel.fournisseur.FournisseurRepository;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.vehicule.Vehicule;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemandeServiceTest {

    @Mock DemandeCarteCarburantRepository demandeRepository;
    @Mock AffectationRepository affectationRepository;
    @Mock CarteCarburantRepository carteRepository;
    @Mock FournisseurRepository fournisseurRepository;
    @Mock UtilisateurRepository utilisateurRepository;
    @InjectMocks DemandeService service;

    private UserPrincipal principal(RoleUtilisateur role, Long provinceId, Long userId) {
        UserPrincipal p = mock(UserPrincipal.class);
        lenient().when(p.getRole()).thenReturn(role);
        lenient().when(p.getProvinceId()).thenReturn(provinceId);
        lenient().when(p.getId()).thenReturn(userId);
        return p;
    }

    private Vehicule vehicule(Long provinceId) {
        Province p = new Province(); p.setId(provinceId); p.setLibelle("P" + provinceId);
        Vehicule v = new Vehicule(); v.setId(9L); v.setImmatriculation("X-9"); v.setProvince(p);
        return v;
    }

    private Affectation affectation(Long id, Long provinceId) {
        Affectation a = new Affectation(); a.setId(id); a.setVehicule(vehicule(provinceId));
        return a;
    }

    private Utilisateur utilisateur(Long id) {
        Utilisateur u = new Utilisateur(); u.setId(id); u.setLogin("u" + id); u.setNom("N"); u.setPrenom("P");
        return u;
    }

    private DemandeCarteCarburant demande(Long id, Affectation a, StatutDemandeCarte statut) {
        DemandeCarteCarburant d = new DemandeCarteCarburant();
        d.setId(id); d.setAffectation(a); d.setStatut(statut); d.setDemandeur(utilisateur(7L));
        return d;
    }

    @Test
    void creer_provincialHorsProvince_leve404() {
        when(affectationRepository.findById(1L)).thenReturn(Optional.of(affectation(1L, 1L)));   // province 1
        assertThatThrownBy(() -> service.creer(new DemandeRequest(1L, "Mission"),
            principal(RoleUtilisateur.RESPONSABLE_PROVINCIAL, 2L, 7L)))                       // provincial de 2
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void creer_affectationADejaCarteActive_leve409() {
        when(affectationRepository.findById(1L)).thenReturn(Optional.of(affectation(1L, 1L)));
        when(carteRepository.existsByAffectationIdAndStatut(1L, StatutCarte.ACTIVE)).thenReturn(true);
        assertThatThrownBy(() -> service.creer(new DemandeRequest(1L, "Mission"),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null, 7L)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void valider_dejaTraitee_leve409() {
        when(demandeRepository.findById(1L)).thenReturn(Optional.of(demande(1L, affectation(1L, 1L), StatutDemandeCarte.VALIDEE)));
        assertThatThrownBy(() -> service.valider(1L, new ValiderRequest("C1", new BigDecimal("3000"), 4L),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null, 1L)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void valider_affectationADejaCarteActive_leve409() {
        Affectation a = affectation(1L, 1L);
        when(demandeRepository.findById(1L)).thenReturn(Optional.of(demande(1L, a, StatutDemandeCarte.EN_ATTENTE)));
        when(carteRepository.existsByNumeroCarte("C1")).thenReturn(false);
        when(carteRepository.existsByAffectationIdAndStatut(1L, StatutCarte.ACTIVE)).thenReturn(true);
        assertThatThrownBy(() -> service.valider(1L, new ValiderRequest("C1", new BigDecimal("3000"), 4L),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null, 1L)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void valider_ok_emetLaCarte() {
        Affectation a = affectation(1L, 1L);
        when(demandeRepository.findById(1L)).thenReturn(Optional.of(demande(1L, a, StatutDemandeCarte.EN_ATTENTE)));
        when(carteRepository.existsByNumeroCarte("C1")).thenReturn(false);
        when(carteRepository.existsByAffectationIdAndStatut(1L, StatutCarte.ACTIVE)).thenReturn(false);
        Fournisseur f = new Fournisseur(); f.setId(4L); f.setRaisonSociale("LocAuto");
        when(fournisseurRepository.findById(4L)).thenReturn(Optional.of(f));
        when(carteRepository.saveAndFlush(any(CarteCarburant.class))).thenAnswer(i -> i.getArgument(0));
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur(1L)));

        var res = service.valider(1L, new ValiderRequest("C1", new BigDecimal("3000"), 4L),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null, 1L));

        assertThat(res.statut()).isEqualTo(StatutDemandeCarte.VALIDEE);
        assertThat(res.validateurLogin()).isEqualTo("u1");
    }

    @Test
    void refuser_ok() {
        when(demandeRepository.findById(1L)).thenReturn(Optional.of(demande(1L, affectation(1L, 1L), StatutDemandeCarte.EN_ATTENTE)));
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur(1L)));
        var res = service.refuser(1L, principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null, 1L));
        assertThat(res.statut()).isEqualTo(StatutDemandeCarte.REFUSEE);
    }
}
