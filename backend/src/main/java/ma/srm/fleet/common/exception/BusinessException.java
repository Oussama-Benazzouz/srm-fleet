package ma.srm.fleet.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception personnalisée représentant une erreur liée aux règles métier.
 */

@Getter
public class BusinessException extends RuntimeException{
    /**
     * Statut HTTP associé à l'erreur métier.
     */
    private final HttpStatus status;

    /**
     * Construit une nouvelle exception métier.
     *
     * @param message description de l'erreur destinée à être retournée au client
     * @param status statut HTTP correspondant au type d'erreur rencontré
     */
    public BusinessException(String message, HttpStatus status){
        super(message);
        this.status = status;
    }

}
