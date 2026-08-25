package ma.srm.fleet.config;

import ma.srm.fleet.admin.utilisateur.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initialise le mot de passe du compte « admin » au premier démarrage.
 *
 * <p>Le seed SQL (Flyway) ne peut pas produire de hash BCrypt : il insère donc un placeholder
 * en clair, remplacé ici par un vrai hash. Le mot de passe initial provient exclusivement de la
 * configuration ({@code app.admin.initial-password} / variable {@code APP_ADMIN_INITIAL_PASSWORD}) :
 * aucun mot de passe par défaut n'est codé en dur, afin qu'un déploiement de production ne crée
 * jamais un compte administrateur au mot de passe connu.</p>
 *
 * <p>Si la propriété n'est pas fournie, le compte reste inutilisable (le placeholder n'est pas un
 * hash valide, donc l'authentification échoue) et un message d'erreur invite à la définir. En
 * développement, une valeur est fournie via {@code application-dev.yml}.</p>
 */
@Component
public class AdminPasswordInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminPasswordInitializer.class);

    /** Valeur exacte insérée par le seed SQL (V6). */
    private static final String PLACEHOLDER = "A_DEFINIR_PHASE_3";

    private final UtilisateurRepository repository;
    private final PasswordEncoder encoder;
    private final String initialPassword;

    public AdminPasswordInitializer(UtilisateurRepository repository, PasswordEncoder encoder,
                                    @Value("${app.admin.initial-password:}") String initialPassword) {
        this.repository = repository;
        this.encoder = encoder;
        this.initialPassword = initialPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        repository.findByLogin("admin").ifPresent(admin -> {
            if (!PLACEHOLDER.equals(admin.getMotDePasse())) {
                return; // déjà initialisé lors d'un démarrage précédent
            }
            if (initialPassword == null || initialPassword.isBlank()) {
                log.error("Compte 'admin' non initialisé : définissez APP_ADMIN_INITIAL_PASSWORD "
                    + "puis redémarrez. Aucun mot de passe par défaut n'est appliqué.");
                return;
            }
            admin.setMotDePasse(encoder.encode(initialPassword));
            repository.save(admin);
            log.info("Mot de passe du compte 'admin' initialisé depuis la configuration. "
                + "Changez-le après la première connexion.");
        });
    }
}
