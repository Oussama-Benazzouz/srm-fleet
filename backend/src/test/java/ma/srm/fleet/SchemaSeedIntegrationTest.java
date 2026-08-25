package ma.srm.fleet;

import java.util.List;
import ma.srm.fleet.admin.utilisateur.UtilisateurRepository;
import ma.srm.fleet.alerte.configuration.ConfigurationAlerte;
import ma.srm.fleet.alerte.configuration.ConfigurationAlerteRepository;
import ma.srm.fleet.alerte.configuration.TypeSourceAlerte;
import ma.srm.fleet.referentiel.departement.DepartementRepository;
import ma.srm.fleet.referentiel.province.ProvinceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/** Vérifie que les migrations Flyway et le seed initial s'appliquent sur un vrai PostgreSQL. */
class SchemaSeedIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired ProvinceRepository provinces;
    @Autowired DepartementRepository departements;
    @Autowired UtilisateurRepository utilisateurs;
    @Autowired ConfigurationAlerteRepository configurations;

    @Test
    void migrations_et_seed_appliques() {
        assertThat(provinces.count()).isPositive();
        assertThat(departements.count()).isPositive();
        assertThat(utilisateurs.findByLogin("admin")).isPresent();
    }

    @Test
    void config_alertes_sans_plafond_carte_apres_V8() {
        List<TypeSourceAlerte> types = configurations.findAll().stream()
            .map(ConfigurationAlerte::getTypeSource)
            .toList();
        assertThat(types)
            .containsExactlyInAnyOrder(TypeSourceAlerte.ASSURANCE, TypeSourceAlerte.VISITE_TECHNIQUE);
    }
}
