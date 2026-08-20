package ma.srm.fleet.alerte.configuration;

import ma.srm.fleet.alerte.configuration.dto.ConfigurationAlerteResponse;

final class ConfigurationAlerteMapper {

    private ConfigurationAlerteMapper() {}

    static ConfigurationAlerteResponse toResponse(ConfigurationAlerte c) {
        return new ConfigurationAlerteResponse(c.getTypeSource(), c.getSeuilJours(), c.getCanalDefaut());
    }
}
