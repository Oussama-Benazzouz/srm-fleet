package ma.srm.fleet.conducteur.conducteur.dto;

import java.util.List;
import ma.srm.fleet.conducteur.permis.dto.PermisResponse;

public record ConducteurResponse(
    Long id, String matricule, String nom, String prenom, String fonction, String groupeSanguin,
    Long provinceId, String provinceLibelle,
    Long departementId, String departementLibelle,
    List<PermisResponse> permis) {}
