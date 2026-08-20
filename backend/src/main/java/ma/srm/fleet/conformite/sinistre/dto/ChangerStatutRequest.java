package ma.srm.fleet.conformite.sinistre.dto;

import jakarta.validation.constraints.NotNull;
import ma.srm.fleet.conformite.sinistre.StatutSinistre;

/** RF-24 : faire avancer le statut d'un sinistre (ouvert → en_cours → clôturé). */
public record ChangerStatutRequest(@NotNull StatutSinistre statut) {}
