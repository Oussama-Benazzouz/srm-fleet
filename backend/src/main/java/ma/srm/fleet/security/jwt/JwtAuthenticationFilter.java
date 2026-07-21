package ma.srm.fleet.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import ma.srm.fleet.security.CustomUserDetailsService;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtre d'authentification JWT, execute une seule fois par requete
 * (OncePerRequestFilter), avant que la requete n'atteigne le controleur.
 *
 * Role : lire le jeton JWT eventuellement present dans l'en-tete
 * Authorization, le valider, puis peupler le SecurityContext avec
 * l'utilisateur correspondant si le jeton est valide. C'est ce mecanisme
 * qui rend l'authentification "stateless" : aucune session n'est
 * maintenue cote serveur, chaque requete se suffit a elle-meme.
 *
 * Ce filtre ne bloque jamais une requete lui-meme (il ne renvoie jamais
 * de 401 directement) : il se contente de peupler ou non le contexte de
 * securite ; c'est la configuration des regles d'acces
 * (SecurityFilterChain / @PreAuthorize) qui decide ensuite si la requete
 * est autorisee a continuer.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    // Injection par constructeur (convention du projet)
    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService uds) {
        this.jwtService = jwtService;
        this.userDetailsService = uds;
    }

    /**
     * Logique principale du filtre, executee pour chaque requete HTTP entrante.
     *
     * Etapes :
     * 1. Recuperer l'en-tete Authorization.
     * 2. Si absent ou mal forme (pas de prefixe "Bearer "), laisser passer
     *    la requete sans authentification (elle sera rejetee plus loin si
     *    l'endpoint le necessite).
     * 3. Sinon, extraire et valider le jeton, puis authentifier
     *    l'utilisateur aupres de Spring Security.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
        throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Pas de jeton fourni ou format invalide : on continue la chaine de
        // filtres sans authentifier — les endpoints publics (ex. /login)
        // doivent rester accessibles sans jeton.
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // Retire le prefixe "Bearer " (7 caracteres) pour ne garder que le jeton
        String token = header.substring(7);

        try {
            String login = jwtService.extractUsername(token);

            // On n'authentifie que si :
            // - le jeton contient bien un login exploitable ;
            // - aucune authentification n'est deja presente dans le contexte
            //   (evite d'ecraser une authentification existante si ce filtre
            //   etait appele plusieurs fois, meme si OncePerRequestFilter
            //   garantit deja une seule execution par requete).
            if (login != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Recharge l'utilisateur complet depuis la base (role,
                // statut du compte, province...), le jeton ne contenant
                // qu'un sous-ensemble d'informations.
                UserDetails user = userDetailsService.loadUserByUsername(login);

                // Construit un objet d'authentification Spring Security
                // "deja authentifie" (credentials a null : le mot de passe
                // n'est plus necessaire, la preuve d'identite est le jeton
                // valide, pas un nouveau controle de mot de passe).
                var authentication = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());

                // Enrichit l'authentification avec des details techniques
                // de la requete (adresse IP, session...), utile pour le
                // logging/audit.
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Publie l'authentification dans le contexte de securite :
                // a partir de ce point, la requete est consideree comme
                // authentifiee pour le reste du traitement.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Jeton invalide, expire, ou signature incorrecte (leve par
            // JwtService.parse) : on ne bloque pas la requete ici, on se
            // contente de nettoyer le contexte pour s'assurer qu'aucune
            // authentification partielle ou incoherente ne subsiste.
            // La requete continuera ensuite comme non authentifiee, et sera
            // rejetee plus loin par les regles d'acces si l'endpoint
            // demande une authentification.
            logger.warn("Echec de validation du token JWT: {}");
            SecurityContextHolder.clearContext();   // token invalide ou expiré
        }

        // Quoi qu'il arrive (authentifie, non authentifie, ou erreur geree),
        // la requete continue son chemin dans la chaine de filtres.
        chain.doFilter(request, response);
    }
}
