package ma.srm.fleet.common.dto;

/**
 * Enveloppe générique utilisée pour standardiser les réponses API.
 *
 * <p>Cette classe permet d'encapsuler les données retournées par les contrôleurs
 * REST dans un format commun pour toute l'application.</p>
 *
 * <p>Elle évite que chaque endpoint définisse son propre format de réponse
 * et garantit une structure homogène côté frontend.</p>
 *
 * @param <T> type des données contenues dans la réponse
 */

public record ApiResponse<T>(T data, String message){

    /**
     * Crée une réponse contenant uniquement les données.
     *
     * @param data données retournées
     * @param <T> type des données
     * @return une réponse API contenant les données
     */
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, null);
    }
}
