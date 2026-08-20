package ma.srm.fleet.alerte.configuration;

import java.util.Comparator;
import java.util.List;
import ma.srm.fleet.alerte.configuration.dto.ConfigurationAlerteResponse;
import ma.srm.fleet.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConfigurationAlerteService {

    private final ConfigurationAlerteRepository repository;

    public ConfigurationAlerteService(ConfigurationAlerteRepository repository) {
        this.repository = repository;
    }

    /** RF-20 : consulter les seuils paramétrés, par type d'échéance surveillé. */
    @Transactional(readOnly = true)
    public List<ConfigurationAlerteResponse> lister() {
        return repository.findAll().stream()
            .sorted(Comparator.comparing(ConfigurationAlerte::getTypeSource))
            .map(ConfigurationAlerteMapper::toResponse)
            .toList();
    }

    /** RF-20 : modifier le seuil (en jours) d'un type, sans intervention technique. */
    public ConfigurationAlerteResponse modifierSeuil(TypeSourceAlerte typeSource, Integer seuilJours) {
        ConfigurationAlerte config = repository.findById(typeSource)
            .orElseThrow(() -> new BusinessException(
                "Configuration d'alerte introuvable : " + typeSource, HttpStatus.NOT_FOUND));
        config.setSeuilJours(seuilJours);
        return ConfigurationAlerteMapper.toResponse(config);
    }
}
