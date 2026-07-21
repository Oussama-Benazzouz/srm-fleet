package ma.srm.fleet.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;

/**
 * Gestion centralisee des exceptions liees a la securite, au meme format
 * RFC 7807 (ProblemDetail) que le GlobalExceptionHandler general du projet
 * (common/error). Ce gestionnaire est dedie specifiquement aux exceptions
 * de Spring Security, qui ne remontent pas necessairement par le meme
 * chemin que les exceptions metier classiques (elles peuvent etre levees
 * avant meme d'atteindre un controleur, au niveau des filtres).
 *
 * Objectif : ne jamais laisser fuiter le detail technique interne d'une
 * exception de securite (nom de la classe Java, message brut de Spring
 * Security) vers le client — uniquement un message clair et volontairement
 * generique, conformement a la bonne pratique deja notee dans
 * CustomUserDetailsService (ne pas reveler si un compte existe).
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityExceptionHandler {

    /**
     * Intercepte tout echec d'authentification (mauvais mot de passe,
     * utilisateur introuvable via UsernameNotFoundException, ou compte
     * desactive via isEnabled() = false dans UserPrincipal).
     *
     * Le message est volontairement generique et regroupe ces trois cas
     * sans les distinguer : cela evite qu'un client puisse deviner, par la
     * difference de message, si un login existe mais avec un mauvais mot
     * de passe, ou s'il n'existe pas du tout, ou si le compte est
     * desactive — meme logique de securite que le login lui-meme.
     *
     * @param ex l'exception d'authentification levee par Spring Security
     * @return une reponse 401 Unauthorized au format RFC 7807
     */
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED, "Identifiants invalides ou compte désactivé");
        pd.setTitle("Authentification échouée");
        return pd;
    }

    /**
     * Intercepte les refus d'acces déclenchés par les règles d'autorisation
     * (@PreAuthorize, restrictions par role ou par province) lorsque
     * l'utilisateur est bien authentifie mais ne possede pas les droits
     * necessaires pour l'action demandee.
     *
     * Exemple concret dans ce projet : un Responsable Provincial qui tente
     * de creer un vehicule (RG12) ou un compte utilisateur (RG14),
     * operations reservees a l'Administrateur Central.
     *
     * @param ex l'exception d'acces refuse levee par Spring Security
     * @return une reponse 403 Forbidden au format RFC 7807
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN, "Accès refusé : droits insuffisants");
        pd.setTitle("Accès refusé");
        return pd;
    }
}
