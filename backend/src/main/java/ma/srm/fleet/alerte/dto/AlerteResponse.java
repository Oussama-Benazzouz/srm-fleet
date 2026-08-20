package ma.srm.fleet.alerte.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import ma.srm.fleet.alerte.StatutLecture;
import ma.srm.fleet.alerte.configuration.TypeSourceAlerte;

public record AlerteResponse(
    Long id,
    TypeSourceAlerte typeSource,
    Long vehiculeId,
    String immatriculation,
    Long provinceId,
    String provinceLibelle,
    LocalDate dateEcheance,
    long joursRestants,
    String conducteurNomComplet,
    StatutLecture statutLecture,
    LocalDateTime dateDeclenchement) {}
