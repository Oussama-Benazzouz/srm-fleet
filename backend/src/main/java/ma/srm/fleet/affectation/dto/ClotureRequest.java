package ma.srm.fleet.affectation.dto;

import java.time.LocalDate;

public record ClotureRequest(LocalDate dateFin) {}   // défaut : aujourd'hui si null
