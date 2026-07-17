package ma.srm.fleet.common.dto;

import java.util.List;

/**
 * Objet de réponse standard utilisé pour représenter les résultats paginés.
 *
 * <p>Cette classe encapsule les informations nécessaires à la gestion de la
 * pagination côté frontend : contenu de la page courante, numéro de page,
 * taille de page et informations globales sur le nombre total d'éléments.</p>
 *
 * <p>Elle permet d'avoir un format uniforme pour toutes les API retournant
 * des listes paginées.</p>
 *
 * @param <T> type des éléments contenus dans la page
 */

public record PageResponse<T>(
  List<T> content,
  int page,
  int size,
  int totalElements,
  int totalPages
) {}
