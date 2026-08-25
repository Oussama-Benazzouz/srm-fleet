package ma.srm.fleet;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base des tests d'intégration : démarre un vrai PostgreSQL via Testcontainers et branche la
 * source de données de l'application dessus. Les migrations Flyway s'appliquent réellement, ce
 * qui permet de valider le schéma, le seed et surtout les contraintes spécifiques à PostgreSQL
 * (index uniques partiels RG3/RG4) — impossibles à vérifier avec une base en mémoire.
 *
 * <p>Conteneur unique (pattern singleton) : démarré une seule fois et réutilisé par toutes les
 * classes de tests d'intégration, puis nettoyé automatiquement à l'arrêt de la JVM par le
 * conteneur de surveillance de Testcontainers.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractPostgresIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
