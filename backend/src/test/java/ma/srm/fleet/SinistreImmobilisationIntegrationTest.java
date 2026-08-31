package ma.srm.fleet;

import java.time.LocalDate;
import ma.srm.fleet.affectation.AffectationService;
import ma.srm.fleet.affectation.dto.AffectationRequest;
import ma.srm.fleet.admin.utilisateur.UtilisateurRepository;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.conducteur.conducteur.Conducteur;
import ma.srm.fleet.conducteur.conducteur.ConducteurRepository;
import ma.srm.fleet.conformite.sinistre.SinistreService;
import ma.srm.fleet.conformite.sinistre.dto.SinistreRequest;
import ma.srm.fleet.referentiel.departement.DepartementRepository;
import ma.srm.fleet.referentiel.province.ProvinceRepository;
import ma.srm.fleet.referentiel.vehicule.ModeAcquisition;
import ma.srm.fleet.referentiel.vehicule.StatutVehicule;
import ma.srm.fleet.referentiel.vehicule.Vehicule;
import ma.srm.fleet.referentiel.vehicule.VehiculeRepository;
import ma.srm.fleet.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Vérifie de bout en bout (PostgreSQL réel) que déclarer un sinistre immobilise le véhicule dans
 * la même transaction, et qu'un véhicule immobilisé n'est pas affectable via le service.
 */
@Transactional
class SinistreImmobilisationIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired SinistreService sinistreService;
    @Autowired AffectationService affectationService;
    @Autowired ProvinceRepository provinces;
    @Autowired DepartementRepository departements;
    @Autowired VehiculeRepository vehicules;
    @Autowired ConducteurRepository conducteurs;
    @Autowired UtilisateurRepository utilisateurs;

    private UserPrincipal admin() {
        return new UserPrincipal(utilisateurs.findByLogin("admin").orElseThrow());
    }

    private Vehicule vehicule(String immatriculation, StatutVehicule statut) {
        Vehicule v = new Vehicule();
        v.setImmatriculation(immatriculation);
        v.setTypeVehicule("CAMION");
        v.setModeAcquisition(ModeAcquisition.PROPRIETE);
        v.setStatut(statut);
        v.setProvince(provinces.findAll().get(0));
        return vehicules.saveAndFlush(v);
    }

    private Conducteur conducteur(String matricule) {
        Conducteur c = new Conducteur();
        c.setMatricule(matricule);
        c.setNom("Test");
        c.setPrenom("Conducteur");
        c.setProvince(provinces.findAll().get(0));
        c.setDepartement(departements.findAll().get(0));
        return conducteurs.saveAndFlush(c);
    }

    @Test
    void declarer_sinistre_immobilise_le_vehicule() {
        Vehicule v = vehicule("SIN-IMM-1", StatutVehicule.DISPONIBLE);

        sinistreService.creer(new SinistreRequest(v.getId(), LocalDate.now(), "Collision"), admin());

        assertThat(vehicules.findById(v.getId()).orElseThrow().getStatut())
            .isEqualTo(StatutVehicule.IMMOBILISE);
    }

    @Test
    void vehicule_immobilise_non_affectable() {
        Vehicule v = vehicule("SIN-IMM-2", StatutVehicule.IMMOBILISE);
        Conducteur c = conducteur("SIN-IMM-C2");

        assertThatThrownBy(() -> affectationService.creer(
            new AffectationRequest(v.getId(), c.getId(), LocalDate.now(), "Mission"), admin()))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }
}
