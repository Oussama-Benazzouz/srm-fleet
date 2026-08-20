package ma.srm.fleet.alerte.engine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import ma.srm.fleet.alerte.Alerte;
import ma.srm.fleet.alerte.AlerteRepository;
import ma.srm.fleet.alerte.StatutLecture;
import ma.srm.fleet.alerte.configuration.CanalAlerte;
import ma.srm.fleet.alerte.configuration.ConfigurationAlerte;
import ma.srm.fleet.alerte.configuration.ConfigurationAlerteRepository;
import ma.srm.fleet.conformite.assurance.Assurance;
import ma.srm.fleet.conformite.assurance.AssuranceRepository;
import ma.srm.fleet.conformite.visitetechnique.VisiteTechnique;
import ma.srm.fleet.conformite.visitetechnique.VisiteTechniqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Génère les alertes pour les échéances qui approchent (RF-20). Pour chaque type surveillé,
 * on retient la dernière échéance par véhicule tombant dans la fenêtre du seuil, et on crée une
 * alerte si aucune n'existe encore pour cette échéance (déduplication par type + référence).
 */
@Service
@Transactional
public class AlerteGenerationService {

    private final ConfigurationAlerteRepository configRepository;
    private final AssuranceRepository assuranceRepository;
    private final VisiteTechniqueRepository visiteRepository;
    private final AlerteRepository alerteRepository;

    public AlerteGenerationService(ConfigurationAlerteRepository configRepository,
                                   AssuranceRepository assuranceRepository,
                                   VisiteTechniqueRepository visiteRepository,
                                   AlerteRepository alerteRepository) {
        this.configRepository = configRepository;
        this.assuranceRepository = assuranceRepository;
        this.visiteRepository = visiteRepository;
        this.alerteRepository = alerteRepository;
    }

    /** Passe en revue toutes les configurations et crée les alertes manquantes. Retourne le nombre créé. */
    public int genererToutes() {
        LocalDate aujourdhui = LocalDate.now();
        int crees = 0;
        for (ConfigurationAlerte config : configRepository.findAll()) {
            LocalDate limite = aujourdhui.plusDays(config.getSeuilJours());
            List<Long> references = switch (config.getTypeSource()) {
                case ASSURANCE -> assuranceRepository.findEcheancesAControler(limite).stream()
                    .map(Assurance::getId).toList();
                case VISITE_TECHNIQUE -> visiteRepository.findEcheancesAControler(limite).stream()
                    .map(VisiteTechnique::getId).toList();
            };
            crees += creerAlertesManquantes(config, references);
        }
        return crees;
    }

    private int creerAlertesManquantes(ConfigurationAlerte config, List<Long> references) {
        int crees = 0;
        for (Long idReference : references) {
            if (alerteRepository.existsByConfiguration_TypeSourceAndIdReference(config.getTypeSource(), idReference)) {
                continue;   // déjà signalée : on ne redéclenche pas chaque jour
            }
            Alerte alerte = new Alerte();
            alerte.setConfiguration(config);
            alerte.setIdReference(idReference);
            alerte.setDateDeclenchement(LocalDateTime.now());
            alerte.setCanal(CanalAlerte.TABLEAU_DE_BORD);   // notifications in-app uniquement
            alerte.setStatutLecture(StatutLecture.NON_LUE);
            alerteRepository.save(alerte);
            crees++;
        }
        return crees;
    }
}
