package ma.srm.fleet.conformite.document.dto;

import java.time.LocalDateTime;

public record DocumentResponse(
    Long id,
    Long sinistreId,
    String nomFichier,
    String typeDocument,
    LocalDateTime dateUpload) {}
