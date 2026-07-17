package ma.srm.fleet.config;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration transverse activant l'auditing JPA (Spring Data).
 *
 * Ce mecanisme alimente automatiquement les champs annotes
 * {@code @CreatedDate}, {@code @LastModifiedDate}, {@code @CreatedBy} et
 * {@code @LastModifiedBy} (voir {@link ma.srm.fleet.common.audit.AuditableEntity})
 * a chaque persistance/modification d'une entite, sans code manuel dans les
 * services metier.
 *
 * Exigence de tracabilite du cahier des charges : toute creation ou
 * modification d'une entite doit pouvoir etre datee et attribuee a un auteur.
 */
@Configuration
// Active le mecanisme d'auditing de Spring Data JPA pour toute l'application.
// Sans cette annotation, les annotations @CreatedDate/@CreatedBy... sur
// AuditableEntity resteraient inertes (jamais renseignees par Spring).
@EnableJpaAuditing
public class JpaAuditingConfig {

    /**
     * Fournit l'identite de "l'auteur courant" utilisee pour renseigner
     * les colonnes created_by / updated_by.
     *
     * Phase 2 (etat actuel) : aucune authentification n'existe encore dans
     * le projet -> on retourne une valeur fixe "system" pour que
     * l'auditing fonctionne des maintenant (colonnes non nulles, tests
     * possibles) sans bloquer sur une dependance a la securite.
     *
     * Phase 3 (a venir) : ce bean sera remplace par une implementation qui
     * lit l'utilisateur authentifie depuis le SecurityContext de Spring
     * Security (ex. utilisateur.getLogin()), sans modifier
     * AuditableEntity ni aucune entite qui en herite - seul ce bean change.
     *
     * @return un fournisseur d'auteur, ici toujours "system"
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        // Optional.of(...) : Spring Data exige un Optional (permet de
        // representer l'absence d'auteur, non utilisee ici pour l'instant).
        return () -> Optional.of("system");
    }
}
