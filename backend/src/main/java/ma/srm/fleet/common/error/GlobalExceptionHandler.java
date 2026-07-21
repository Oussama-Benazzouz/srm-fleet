package ma.srm.fleet.common.error;

import ma.srm.fleet.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * Gestionnaire global des exceptions de l'application.
 * <p>Cette classe centralise la gestion des erreurs provenant des contrôleurs REST
 * grâce à l'annotation {@link RestControllerAdvice}.</p>
 *
 * <p>Elle intercepte les exceptions levées dans toute l'application et les
 * transforme en réponses HTTP standardisées utilisant {@link ProblemDetail},
 * conformément à la spécification RFC 7807.</p>
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    /**
     * Traite les exceptions liées aux règles métier.
     *
     * <p>Transforme une {@link BusinessException} en une réponse d'erreur
     * standardisée RFC 7807 contenant le statut HTTP approprié ainsi que
     * le message explicatif associé.</p>
     *
     * @param ex exception métier interceptée
     * @return une réponse d'erreur standardisée
     */
    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex){
        log.warn("Erreur métier: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problemDetail.setTitle("Erreur Métier");
        return problemDetail;
    }

    /**
     * Traite les erreurs de validation des données entrantes.
     *
     * <p>Cette exception est levée automatiquement par Spring lorsqu'un objet
     * reçu dans une requête HTTP ne respecte pas les contraintes de validation
     * définies avec les annotations comme {@code @NotNull}, {@code @NotBlank},
     * {@code @Size}, etc.</p>
     *
     * @param ex exception de validation contenant les erreurs détectées
     * @return une réponse indiquant que les données fournies sont invalides
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex){
        log.warn("Erreur de validation: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
            "Un ou plusieurs champs sont invalides");
        problemDetail.setTitle("Erreur de validation");
        return problemDetail;
    }

    /**
     * Traite les exceptions non prévues par les gestionnaires spécifiques.
     *
     * <p>Cette méthode sert de gestionnaire de dernier niveau pour toutes les
     * erreurs inattendues qui peuvent survenir dans l'application.</p>
     *
     * <p>Les détails techniques de l'exception ne sont volontairement pas
     * retournés au client afin d'éviter l'exposition d'informations sensibles.
     * Ces informations doivent être enregistrées dans les logs côté serveur.</p>
     *
     * @param ex exception inattendue rencontrée
     * @return une réponse d'erreur interne standardisée
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex){
        log.error("Erreur inattendue", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Une erreur inattendue est survenue."
        );
        problemDetail.setTitle("Erreur interne");
        return problemDetail;
    }
}
