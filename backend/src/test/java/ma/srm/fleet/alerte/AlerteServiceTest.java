package ma.srm.fleet.alerte;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.alerte.configuration.CanalAlerte;
import ma.srm.fleet.alerte.configuration.ConfigurationAlerte;
import ma.srm.fleet.alerte.configuration.TypeSourceAlerte;
import ma.srm.fleet.alerte.dto.AlerteResponse;
import ma.srm.fleet.conducteur.conducteur.Conducteur;
import ma.srm.fleet.conformite.assurance.Assurance;
import ma.srm.fleet.conformite.assurance.AssuranceRepository;
import ma.srm.fleet.conformite.visitetechnique.VisiteTechnique;
import ma.srm.fleet.conformite.visitetechnique.VisiteTechniqueRepository;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.vehicule.Vehicule;
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
class AlerteServiceTest {

    @Mock AlerteRepository alerteRepository;
    @Mock AssuranceRepository assuranceRepository;
    @Mock VisiteTechniqueRepository visiteRepository;
    @InjectMocks AlerteService service;

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

    private Alerte alerte(Long id, TypeSourceAlerte type, Long idRef) {
        ConfigurationAlerte cfg = new ConfigurationAlerte();
        cfg.setTypeSource(type); cfg.setSeuilJours(30); cfg.setCanalDefaut(CanalAlerte.TABLEAU_DE_BORD);
        Alerte a = new Alerte();
        a.setId(id); a.setConfiguration(cfg); a.setIdReference(idRef);
        a.setStatutLecture(StatutLecture.NON_LUE); a.setDateDeclenchement(LocalDateTime.now());
        return a;
    }

    private Assurance assurance(long joursAvantEcheance) {
        Assurance a = new Assurance();
        a.setId(5L); a.setVehicule(vehicule(1L)); a.setDateEcheance(LocalDate.now().plusDays(joursAvantEcheance));
        return a;
    }

    private VisiteTechnique visite(long joursAvantEcheance) {
        Conducteur c = new Conducteur(); c.setId(3L); c.setNom("Alaoui"); c.setPrenom("Sara"); c.setMatricule("M-3");
        VisiteTechnique v = new VisiteTechnique();
        v.setId(7L); v.setVehicule(vehicule(1L)); v.setDateEcheance(LocalDate.now().plusDays(joursAvantEcheance));
        v.setConducteurAccompagnateur(c);
        return v;
    }

    @Test
    void lister_trie_par_urgence_echeance_la_plus_proche_dabord() {
        when(alerteRepository.findByStatutLecture(StatutLecture.NON_LUE)).thenReturn(List.of(
            alerte(1L, TypeSourceAlerte.ASSURANCE, 5L),
            alerte(2L, TypeSourceAlerte.VISITE_TECHNIQUE, 7L)));
        when(assuranceRepository.findById(5L)).thenReturn(Optional.of(assurance(10)));
        when(visiteRepository.findById(7L)).thenReturn(Optional.of(visite(2)));

        List<AlerteResponse> res = service.lister(principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null));

        assertThat(res).hasSize(2);
        assertThat(res.get(0).typeSource()).isEqualTo(TypeSourceAlerte.VISITE_TECHNIQUE);
        assertThat(res.get(0).joursRestants()).isEqualTo(2);
        assertThat(res.get(1).joursRestants()).isEqualTo(10);
    }

    @Test
    void lister_provincial_ne_voit_pas_hors_province() {
        when(alerteRepository.findByStatutLecture(StatutLecture.NON_LUE))
            .thenReturn(List.of(alerte(1L, TypeSourceAlerte.ASSURANCE, 5L)));
        when(assuranceRepository.findById(5L)).thenReturn(Optional.of(assurance(10)));   // véhicule province 1

        List<AlerteResponse> res = service.lister(principal(RoleUtilisateur.RESPONSABLE_PROVINCIAL, 2L));

        assertThat(res).isEmpty();
    }

    @Test
    void marquerLue_ok() {
        when(alerteRepository.findById(1L)).thenReturn(Optional.of(alerte(1L, TypeSourceAlerte.ASSURANCE, 5L)));
        when(assuranceRepository.findById(5L)).thenReturn(Optional.of(assurance(10)));

        AlerteResponse res = service.marquerLue(1L, principal(RoleUtilisateur.ADMINISTRATEUR_CENTRAL, null));

        assertThat(res.statutLecture()).isEqualTo(StatutLecture.LUE);
    }
}
