package ma.srm.fleet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Active l'ordonnanceur Spring pour le moteur d'alertes (RF-20/RF-22).
 *
 * <p>La tâche planifiée surveille les échéances indépendamment de toute connexion utilisateur ;
 * sa fréquence est pilotée par la propriété {@code app.alertes.cron}.</p>
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
}
