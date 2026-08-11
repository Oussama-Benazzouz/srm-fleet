package ma.srm.fleet.affectation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.affectation.dto.AffectationFilter;
import ma.srm.fleet.affectation.dto.AffectationRequest;
import ma.srm.fleet.affectation.dto.ClotureRequest;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.conducteur.conducteur.Conducteur;
import ma.srm.fleet.conducteur.conducteur.ConducteurRepository;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.vehicule.StatutVehicule;
import ma.srm.fleet.referentiel.vehicule.Vehicule;
import ma.srm.fleet.referentiel.vehicule.VehiculeRepository;
import ma.srm.fleet.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service d'affectation (Mockito pur, sans contexte Spring).
 * Couvre : création (RG3, cohérence province, portée provinciale, synchro statut,
 * concurrence), clôture (RF-08/09, préservation d'un statut non-AFFECTE, portée),
 * historiques véhicule/conducteur (RF-10, portée) et liste filtrée (portée + garde).
 */
@ExtendWith(MockitoExtension.class)
class AffectationServiceTest {

    @Mock AffectationRepository affectationRepository;
    @Mock VehiculeRepository vehiculeRepository;
    @Mock ConducteurRepository conducteurRepository;
    @InjectMocks AffectationService service;

    // --- Fabriques d'objets de test ---

    private UserPrincipal principal(RoleUtilisateur role, Long provinceId) {
        UserPrincipal p = mock(UserPrincipal.class);
        lenient().when(p.getRole()).thenReturn(role);
        lenient().when(p.getProvinceId()).thenReturn(provinceId);
        return p;
    }

    private Province province(Long id) {
        Province p = new Province();
        p.setId(id);
        p.setLibelle("Province " + id);
        return p;
    }

    private Vehicule vehicule(Long id, Long provinceId, StatutVehicule statut) {
        Vehicule v = new Vehicule();
        v.setId(id);
        v.setImmatriculation("X-" + id);
        v.setProvince(province(provinceId));
        v.setStatut(statut);
        return v;
    }

    private Conducteur conducteur(Long id, Long provinceId) {
        Conducteur c = new Conducteur();
        c.setId(id);
        c.setNom("Nom");
        c.setPrenom("Prenom");
        c.setMatricule("C" + id);
        c.setProvince(province(provinceId));
        return c;
    }

    private Affectation affectation(Long id, Vehicule v, Conducteur c, StatutAffectation statut) {
        Affectation a = new Affectation();
        a.setId(id);
        a.setVehicule(v);
        a.setConducteur(c);
        a.setDateDebut(LocalDate.now().minusDays(5));
        a.setStatut(statut);
        return a;
    }

    // --- Création ---

    @Test
    void creer_ok_vehiculePasseAffecte() {
        Vehicule v = vehicule(1L, 1L, StatutVehicule.DISPONIBLE);
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(v));
        when(conducteurRepository.findById(2L)).thenReturn(Optional.of(conducteur(2L, 1L)));
        when(affectationRepository.existsByVehiculeIdAndStatut(1L, StatutAffectation.ACTIVE)).thenReturn(false);
        when(affectationRepository.saveAndFlush(any())).thenAnswer(i -> {
            Affectation a = i.getArgument(0);
            a.setId(9L);
            return a;
        });

        var res = service.creer(new AffectationRequest(1L, 2L, null, "Mission"),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null));

        assertThat(res.statut()).isEqualTo(StatutAffectation.ACTIVE);
        assertThat(res.motif()).isEqualTo("Mission");
        assertThat(v.getStatut()).isEqualTo(StatutVehicule.AFFECTE);
    }

    @Test
    void creer_vehiculeDejaAffecte_leve409() {
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule(1L, 1L, StatutVehicule.DISPONIBLE)));
        when(conducteurRepository.findById(2L)).thenReturn(Optional.of(conducteur(2L, 1L)));
        when(affectationRepository.existsByVehiculeIdAndStatut(1L, StatutAffectation.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> service.creer(new AffectationRequest(1L, 2L, null, null),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void creer_vehiculeNonDisponible_leve409() {
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule(1L, 1L, StatutVehicule.IMMOBILISE)));
        when(conducteurRepository.findById(2L)).thenReturn(Optional.of(conducteur(2L, 1L)));
        when(affectationRepository.existsByVehiculeIdAndStatut(1L, StatutAffectation.ACTIVE)).thenReturn(false);

        assertThatThrownBy(() -> service.creer(new AffectationRequest(1L, 2L, null, null),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void creer_provincesDifferentes_leve400() {
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule(1L, 1L, StatutVehicule.DISPONIBLE)));
        when(conducteurRepository.findById(2L)).thenReturn(Optional.of(conducteur(2L, 2L)));   // province différente

        assertThatThrownBy(() -> service.creer(new AffectationRequest(1L, 2L, null, null),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void creer_responsableProvincialHorsProvince_leve404() {
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule(1L, 1L, StatutVehicule.DISPONIBLE)));
        when(conducteurRepository.findById(2L)).thenReturn(Optional.of(conducteur(2L, 1L)));

        assertThatThrownBy(() -> service.creer(new AffectationRequest(1L, 2L, null, null),
            principal(RoleUtilisateur.RESPONSABLE_PROVINCIAL, 2L)))   // responsable de la province 2
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void creer_concurrence_indexPartiel_leve409() {
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule(1L, 1L, StatutVehicule.DISPONIBLE)));
        when(conducteurRepository.findById(2L)).thenReturn(Optional.of(conducteur(2L, 1L)));
        when(affectationRepository.existsByVehiculeIdAndStatut(1L, StatutAffectation.ACTIVE)).thenReturn(false);
        when(affectationRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("dup"));

        assertThatThrownBy(() -> service.creer(new AffectationRequest(1L, 2L, null, null),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    // --- Clôture ---

    @Test
    void cloturer_ok_vehiculeRedevientDisponible() {
        Vehicule v = vehicule(1L, 1L, StatutVehicule.AFFECTE);
        Affectation a = affectation(9L, v, conducteur(2L, 1L), StatutAffectation.ACTIVE);
        when(affectationRepository.findById(9L)).thenReturn(Optional.of(a));

        var res = service.cloturer(9L, new ClotureRequest(null),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null));

        assertThat(res.statut()).isEqualTo(StatutAffectation.CLOTUREE);
        assertThat(res.dateFin()).isNotNull();
        assertThat(v.getStatut()).isEqualTo(StatutVehicule.DISPONIBLE);
    }

    @Test
    void cloturer_dejaCloturee_leve409() {
        Affectation a = affectation(9L, vehicule(1L, 1L, StatutVehicule.DISPONIBLE),
            conducteur(2L, 1L), StatutAffectation.CLOTUREE);
        when(affectationRepository.findById(9L)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> service.cloturer(9L, new ClotureRequest(null),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void cloturer_dateFinAvantDebut_leve400() {
        Affectation a = affectation(9L, vehicule(1L, 1L, StatutVehicule.AFFECTE),
            conducteur(2L, 1L), StatutAffectation.ACTIVE);
        a.setDateDebut(LocalDate.now());
        when(affectationRepository.findById(9L)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> service.cloturer(9L, new ClotureRequest(LocalDate.now().minusDays(1)),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void cloturer_vehiculeImmobilise_conserveStatut() {
        Vehicule v = vehicule(1L, 1L, StatutVehicule.IMMOBILISE);
        Affectation a = affectation(9L, v, conducteur(2L, 1L), StatutAffectation.ACTIVE);
        when(affectationRepository.findById(9L)).thenReturn(Optional.of(a));

        var res = service.cloturer(9L, new ClotureRequest(null),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null));

        assertThat(res.statut()).isEqualTo(StatutAffectation.CLOTUREE);
        assertThat(v.getStatut()).isEqualTo(StatutVehicule.IMMOBILISE);   // statut non-AFFECTE préservé
    }

    @Test
    void cloturer_responsableProvincialHorsProvince_leve404() {
        Affectation a = affectation(9L, vehicule(1L, 1L, StatutVehicule.AFFECTE),
            conducteur(2L, 1L), StatutAffectation.ACTIVE);
        when(affectationRepository.findById(9L)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> service.cloturer(9L, new ClotureRequest(null),
            principal(RoleUtilisateur.RESPONSABLE_PROVINCIAL, 2L)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // --- Historique véhicule ---

    @Test
    void historiqueVehicule_ok() {
        Vehicule v = vehicule(1L, 1L, StatutVehicule.AFFECTE);
        Affectation a = affectation(9L, v, conducteur(2L, 1L), StatutAffectation.ACTIVE);
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(v));
        when(affectationRepository.findByVehiculeIdOrderByDateDebutDesc(1L)).thenReturn(List.of(a));

        var res = service.historiqueVehicule(1L, principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null));

        assertThat(res).hasSize(1);
        assertThat(res.get(0).vehiculeId()).isEqualTo(1L);
    }

    @Test
    void historiqueVehicule_horsProvince_leve404() {
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule(1L, 1L, StatutVehicule.AFFECTE)));

        assertThatThrownBy(() -> service.historiqueVehicule(1L,
            principal(RoleUtilisateur.RESPONSABLE_PROVINCIAL, 2L)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void historiqueVehicule_introuvable_leve404() {
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.historiqueVehicule(1L,
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // --- Historique conducteur ---

    @Test
    void historiqueConducteur_ok() {
        Conducteur c = conducteur(2L, 1L);
        Affectation a = affectation(9L, vehicule(1L, 1L, StatutVehicule.AFFECTE), c, StatutAffectation.ACTIVE);
        when(conducteurRepository.findById(2L)).thenReturn(Optional.of(c));
        when(affectationRepository.findByConducteurIdOrderByDateDebutDesc(2L)).thenReturn(List.of(a));

        var res = service.historiqueConducteur(2L, principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null));

        assertThat(res).hasSize(1);
        assertThat(res.get(0).conducteurId()).isEqualTo(2L);
    }

    @Test
    void historiqueConducteur_horsProvince_leve404() {
        when(conducteurRepository.findById(2L)).thenReturn(Optional.of(conducteur(2L, 1L)));

        assertThatThrownBy(() -> service.historiqueConducteur(2L,
            principal(RoleUtilisateur.RESPONSABLE_PROVINCIAL, 2L)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void historiqueConducteur_introuvable_leve404() {
        when(conducteurRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.historiqueConducteur(2L,
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // --- Liste filtrée + portée ---

    @Test
    @SuppressWarnings("unchecked")
    void lister_admin_retourneTout() {
        Affectation a = affectation(9L, vehicule(1L, 1L, StatutVehicule.AFFECTE),
            conducteur(2L, 1L), StatutAffectation.ACTIVE);
        when(affectationRepository.findAll(any(Specification.class))).thenReturn(List.of(a));

        var res = service.lister(new AffectationFilter(null, null, null),
            principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null));

        assertThat(res).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void lister_provincialAvecProvince_retourneListe() {
        Affectation a = affectation(9L, vehicule(1L, 1L, StatutVehicule.AFFECTE),
            conducteur(2L, 1L), StatutAffectation.ACTIVE);
        when(affectationRepository.findAll(any(Specification.class))).thenReturn(List.of(a));

        var res = service.lister(new AffectationFilter(StatutAffectation.ACTIVE, null, null),
            principal(RoleUtilisateur.RESPONSABLE_PROVINCIAL, 1L));

        assertThat(res).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void lister_provincialSansProvince_retourneVide() {
        var res = service.lister(new AffectationFilter(null, null, null),
            principal(RoleUtilisateur.RESPONSABLE_PROVINCIAL, null));

        assertThat(res).isEmpty();
        verify(affectationRepository, never()).findAll(any(Specification.class));
    }
}
