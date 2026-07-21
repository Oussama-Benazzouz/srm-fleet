package ma.srm.fleet.config;

import ma.srm.fleet.admin.utilisateur.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tache d'initialisation executee automatiquement au demarrage de
 * l'application (ApplicationRunner), une fois le contexte Spring
 * entierement charge.
 *
 * Probleme resolu : le seed de donnees (V6, cote Taki) insere
 * probablement le compte "admin" directement en SQL, dans une migration
 * Flyway — or Flyway ne peut pas invoquer BCryptPasswordEncoder (c'est une
 * dependance Java, pas disponible en SQL brut). Le mot de passe est donc
 * seede avec un placeholder en clair (PLACEHOLDER), et cette classe se
 * charge de le remplacer par un veritable hash BCrypt des le premier
 * demarrage du backend, en dehors du domaine de Flyway.
 *
 * Ne s'execute qu'une seule fois utilement : si le mot de passe a deja ete
 * remplace lors d'un demarrage precedent, la condition sur PLACEHOLDER ne
 * correspond plus, et rien ne se passe aux demarrages suivants.
 */
@Component
public class AdminPasswordInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminPasswordInitializer.class);

    // Valeur exacte inseree par le script de seed SQL (V6), a synchroniser
    // avec Taki si elle change de ce cote-la.
    private static final String PLACEHOLDER = "A_DEFINIR_PHASE_3";

    // Mot de passe par defaut du compte admin, uniquement pour
    // l'environnement de developpement — voir avertissement ci-dessous.
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";

    private final UtilisateurRepository repository;
    private final PasswordEncoder encoder;

    // Injection par constructeur (convention du projet)
    public AdminPasswordInitializer(UtilisateurRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    /**
     * Point d'entree appele automatiquement par Spring Boot juste apres le
     * demarrage complet de l'application.
     *
     * @Transactional : necessaire car repository.save(admin) modifie une
     * entite geree par Hibernate ; garantit que la modification est bien
     * validee (commit) en base.
     *
     * Logique : recherche le compte "admin", et seulement si son mot de
     * passe est encore le placeholder brut, le remplace par un hash BCrypt
     * du mot de passe par defaut. Si le compte "admin" n'existe pas encore
     * (findByLogin renvoie Optional.empty()), rien ne se produit — evite
     * une erreur si le seed n'a pas encore ete applique.
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        repository.findByLogin("admin").ifPresent(admin -> {
            if (PLACEHOLDER.equals(admin.getMotDePasse())) {
                admin.setMotDePasse(encoder.encode(DEFAULT_ADMIN_PASSWORD));
                repository.save(admin);
                log.warn("Mot de passe admin initialise a la valeur par defaut '{}'. A CHANGER en production.",
                    DEFAULT_ADMIN_PASSWORD);
            }
        });
    }
}
