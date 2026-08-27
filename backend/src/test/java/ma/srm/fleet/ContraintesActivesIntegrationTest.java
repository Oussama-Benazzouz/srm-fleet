package ma.srm.fleet;

import java.math.BigDecimal;
import java.time.LocalDate;
import ma.srm.fleet.admin.utilisateur.Utilisateur;
import ma.srm.fleet.admin.utilisateur.UtilisateurRepository;
import ma.srm.fleet.affectation.Affectation;
import ma.srm.fleet.affectation.AffectationRepository;
import ma.srm.fleet.affectation.StatutAffectation;
import ma.srm.fleet.carte.cartecarburant.CarteCarburant;
import ma.srm.fleet.carte.cartecarburant.CarteCarburantRepository;
import ma.srm.fleet.carte.cartecarburant.StatutCarte;
import ma.srm.fleet.carte.demande.DemandeCarteCarburant;
import ma.srm.fleet.carte.demande.DemandeCarteCarburantRepository;
import ma.srm.fleet.carte.demande.StatutDemandeCarte;
import ma.srm.fleet.conducteur.conducteur.Conducteur;
import ma.srm.fleet.conducteur.conducteur.ConducteurRepository;
import ma.srm.fleet.referentiel.departement.Departement;
import ma.srm.fleet.referentiel.departement.DepartementRepository;
import ma.srm.fleet.referentiel.fournisseur.Fournisseur;
import ma.srm.fleet.referentiel.fournisseur.FournisseurRepository;
import ma.srm.fleet.referentiel.fournisseur.TypeFournisseur;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Vérifie que les index uniques partiels PostgreSQL rejettent une seconde ressource active,
 * garantissant RG3 (une affectation active par véhicule) et RG4 (une carte active par
 * affectation) au niveau base, indépendamment du contrôle applicatif.
 */
@Transactional
class ContraintesActivesIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired ProvinceRepository provinces;
    @Autowired DepartementRepository departements;
    @Autowired VehiculeRepository vehicules;
    @Autowired ConducteurRepository conducteurs;
    @Autowired AffectationRepository affectations;
    @Autowired FournisseurRepository fournisseurs;
    @Autowired UtilisateurRepository utilisateurs;
    @Autowired DemandeCarteCarburantRepository demandes;
    @Autowired CarteCarburantRepository cartes;

    @Test
    void rg3_deuxieme_affectation_active_sur_le_meme_vehicule_est_rejetee() {
        Province p = provinces.findAll().get(0);
        Departement d = departements.findAll().get(0);
        Vehicule v = vehicules.saveAndFlush(vehicule("RG3-V1", p));
        Conducteur c1 = conducteurs.saveAndFlush(conducteur("RG3-C1", p, d));
        Conducteur c2 = conducteurs.saveAndFlush(conducteur("RG3-C2", p, d));

        affectations.saveAndFlush(affectationActive(v, c1)); // 1re active : acceptée

        assertThatThrownBy(() -> affectations.saveAndFlush(affectationActive(v, c2)))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rg4_deuxieme_carte_active_sur_la_meme_affectation_est_rejetee() {
        Province p = provinces.findAll().get(0);
        Departement d = departements.findAll().get(0);
        Utilisateur admin = utilisateurs.findByLogin("admin").orElseThrow();
        Fournisseur f = fournisseurs.saveAndFlush(fournisseur());
        Vehicule v = vehicules.saveAndFlush(vehicule("RG4-V1", p));
        Conducteur c = conducteurs.saveAndFlush(conducteur("RG4-C1", p, d));
        Affectation a = affectations.saveAndFlush(affectationActive(v, c));
        DemandeCarteCarburant dem = demandes.saveAndFlush(demandeValidee(a, admin));

        cartes.saveAndFlush(carteActive("RG4-CARTE-1", a, f, dem)); // 1re active : acceptée

        assertThatThrownBy(() -> cartes.saveAndFlush(carteActive("RG4-CARTE-2", a, f, dem)))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Vehicule vehicule(String immatriculation, Province p) {
        Vehicule v = new Vehicule();
        v.setImmatriculation(immatriculation);
        v.setTypeVehicule("CAMION");
        v.setModeAcquisition(ModeAcquisition.PROPRIETE);
        v.setStatut(StatutVehicule.DISPONIBLE);
        v.setProvince(p);
        return v;
    }

    private Conducteur conducteur(String matricule, Province p, Departement d) {
        Conducteur c = new Conducteur();
        c.setMatricule(matricule);
        c.setNom("Test");
        c.setPrenom("Conducteur");
        c.setProvince(p);
        c.setDepartement(d);
        return c;
    }

    private Affectation affectationActive(Vehicule v, Conducteur c) {
        Affectation a = new Affectation();
        a.setDateDebut(LocalDate.now());
        a.setStatut(StatutAffectation.ACTIVE);
        a.setVehicule(v);
        a.setConducteur(c);
        return a;
    }

    private Fournisseur fournisseur() {
        Fournisseur f = new Fournisseur();
        f.setRaisonSociale("TotalEnergies");
        f.setType(TypeFournisseur.CARBURANT);
        return f;
    }

    private DemandeCarteCarburant demandeValidee(Affectation a, Utilisateur demandeur) {
        DemandeCarteCarburant dem = new DemandeCarteCarburant();
        dem.setDateDemande(LocalDate.now());
        dem.setStatut(StatutDemandeCarte.VALIDEE);
        dem.setAffectation(a);
        dem.setDemandeur(demandeur);
        return dem;
    }

    private CarteCarburant carteActive(String numero, Affectation a, Fournisseur f, DemandeCarteCarburant dem) {
        CarteCarburant carte = new CarteCarburant();
        carte.setNumeroCarte(numero);
        carte.setPlafondMontant(new BigDecimal("3000"));
        carte.setStatut(StatutCarte.ACTIVE);
        carte.setDateCreation(LocalDate.now());
        carte.setAffectation(a);
        carte.setFournisseur(f);
        carte.setDemandeOrigine(dem);
        return carte;
    }
}
