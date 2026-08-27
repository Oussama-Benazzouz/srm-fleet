package ma.srm.fleet.alerte.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Déclencheur planifié du moteur d'alertes : s'exécute selon {@code app.alertes.cron},
 * indépendamment de toute connexion utilisateur (exigence de fiabilité du cahier des charges).
 */
@Component
public class AlerteEngine {

    private static final Logger log = LoggerFactory.getLogger(AlerteEngine.class);

    private final AlerteGenerationService generationService;

    public AlerteEngine(AlerteGenerationService generationService) {
        this.generationService = generationService;
    }

    @Scheduled(cron = "${app.alertes.cron}")
    public void executer() {
        int crees = generationService.genererToutes();
        log.info("Moteur d'alertes exécuté : {} nouvelle(s) alerte(s) créée(s)", crees);
    }
}
