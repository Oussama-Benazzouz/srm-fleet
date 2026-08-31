package ma.srm.fleet;

import java.time.LocalDate;
import ma.srm.fleet.conducteur.conducteur.Conducteur;
import ma.srm.fleet.conducteur.conducteur.ConducteurRepository;
import ma.srm.fleet.conformite.assurance.Assurance;
import ma.srm.fleet.conformite.assurance.AssuranceRepository;
import ma.srm.fleet.conformite.visitetechnique.VisiteTechnique;
import ma.srm.fleet.conformite.visitetechnique.VisiteTechniqueRepository;
import ma.srm.fleet.referentiel.departement.Departement;
import ma.srm.fleet.referentiel.departement.DepartementRepository;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.province.ProvinceRepository;
import ma.srm.fleet.referentiel.vehicule.ModeAcquisition;
import ma.srm.fleet.referentiel.vehicule.StatutVehicule;
import ma.srm.fleet.referentiel.vehicule.Vehicule;
import ma.srm.fleet.referentiel.vehicule.VehiculeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Vérifie les contraintes PostgreSQL de la migration V9 (filet de sécurité, indépendant du
 * contrôle applicatif) : ordre des dates (CHECK) et non-chevauchement des périodes par véhicule
 * (EXCLUDE USING gist), pour l'assurance et la visite technique.
 */
@Transactional
class ConformitePeriodesIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired ProvinceRepository provinces;
    @Autowired DepartementRepository departements;
    @Autowired VehiculeRepository vehicules;
    @Autowired ConducteurRepository conducteurs;
    @Autowired AssuranceRepository assurances;
    @Autowired VisiteTechniqueRepository visites;

    private Vehicule nouveauVehicule(String immatriculation) {
        Vehicule v = new Vehicule();
        v.setImmatriculation(immatriculation);
        v.setTypeVehicule("CAMION");
        v.setModeAcquisition(ModeAcquisition.PROPRIETE);
        v.setStatut(StatutVehicule.DISPONIBLE);
        v.setProvince(provinces.findAll().get(0));
        return vehicules.saveAndFlush(v);
    }

    private Conducteur nouveauConducteur(String matricule) {
        Conducteur c = new Conducteur();
        c.setMatricule(matricule);
        c.setNom("Test");
        c.setPrenom("Conducteur");
        c.setProvince(provinces.findAll().get(0));
        c.setDepartement(departements.findAll().get(0));
        return conducteurs.saveAndFlush(c);
    }

    private Assurance assurance(Vehicule v, String debut, String echeance) {
        Assurance a = new Assurance();
        a.setVehicule(v);
        a.setNomCompagnie("AXA");
        a.setDateDebut(LocalDate.parse(debut));
        a.setDateEcheance(LocalDate.parse(echeance));
        return a;
    }

    private VisiteTechnique visite(Vehicule v, Conducteur c, String visiteDate, String echeance) {
        VisiteTechnique vt = new VisiteTechnique();
        vt.setVehicule(v);
        vt.setConducteurAccompagnateur(c);
        vt.setNomCompagnie("Norisko");
        vt.setDateVisite(LocalDate.parse(visiteDate));
        vt.setDateEcheance(LocalDate.parse(echeance));
        return vt;
    }

    // --- Assurance ---

    @Test
    void assurance_echeance_avant_debut_rejetee_par_check() {
        Vehicule v = nouveauVehicule("CHK-A1");
        assertThatThrownBy(() -> assurances.saveAndFlush(assurance(v, "2026-08-15", "2026-08-10")))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void assurance_chevauchement_rejete() {
        Vehicule v = nouveauVehicule("OVL-A1");
        assurances.saveAndFlush(assurance(v, "2026-01-01", "2026-06-30"));
        assertThatThrownBy(() -> assurances.saveAndFlush(assurance(v, "2026-06-01", "2026-12-31")))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void assurance_frontiere_partagee_rejetee() {
        Vehicule v = nouveauVehicule("BND-A1");
        assurances.saveAndFlush(assurance(v, "2026-01-01", "2026-03-31"));
        // le 31/03 est couvert par les deux (bornes inclusives) → chevauchement
        assertThatThrownBy(() -> assurances.saveAndFlush(assurance(v, "2026-03-31", "2026-06-30")))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void assurance_periodes_disjointes_acceptees() {
        Vehicule v = nouveauVehicule("GAP-A1");
        assurances.saveAndFlush(assurance(v, "2026-01-01", "2026-03-31"));
        assertThatCode(() -> assurances.saveAndFlush(assurance(v, "2026-04-01", "2026-06-30")))
            .doesNotThrowAnyException();
    }

    @Test
    void assurance_meme_periode_vehicules_differents_acceptee() {
        Vehicule v1 = nouveauVehicule("MUL-A1");
        Vehicule v2 = nouveauVehicule("MUL-A2");
        assurances.saveAndFlush(assurance(v1, "2026-01-01", "2026-12-31"));
        assertThatCode(() -> assurances.saveAndFlush(assurance(v2, "2026-01-01", "2026-12-31")))
            .doesNotThrowAnyException();
    }

    // --- Visite technique ---

    @Test
    void visite_echeance_avant_visite_rejetee_par_check() {
        Vehicule v = nouveauVehicule("CHK-V1");
        Conducteur c = nouveauConducteur("CHK-VC1");
        assertThatThrownBy(() -> visites.saveAndFlush(visite(v, c, "2026-08-15", "2026-08-10")))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void visite_chevauchement_rejete() {
        Vehicule v = nouveauVehicule("OVL-V1");
        Conducteur c = nouveauConducteur("OVL-VC1");
        visites.saveAndFlush(visite(v, c, "2026-01-01", "2026-06-30"));
        assertThatThrownBy(() -> visites.saveAndFlush(visite(v, c, "2026-06-01", "2026-12-31")))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
