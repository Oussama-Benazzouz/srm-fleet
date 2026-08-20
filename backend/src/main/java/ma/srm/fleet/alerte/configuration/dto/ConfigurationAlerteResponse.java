package ma.srm.fleet.alerte.configuration.dto;

import ma.srm.fleet.alerte.configuration.CanalAlerte;
import ma.srm.fleet.alerte.configuration.TypeSourceAlerte;

public record ConfigurationAlerteResponse(
    TypeSourceAlerte typeSource,
    Integer seuilJours,
    CanalAlerte canalDefaut) {}
