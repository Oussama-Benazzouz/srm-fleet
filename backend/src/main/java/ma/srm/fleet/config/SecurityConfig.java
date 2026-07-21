package ma.srm.fleet.config;

import ma.srm.fleet.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration centrale de Spring Security pour le projet.
 *
 * Regroupe en un seul endroit les trois piliers de la securite de
 * l'application : la chaine de filtres HTTP (quels endpoints sont publics,
 * lesquels exigent une authentification), l'encodage des mots de passe, et
 * l'AuthenticationManager utilise lors du login.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    // Injection par constructeur (convention du projet)
    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * Definit la chaine de filtres de securite appliquee a toutes les
     * requetes HTTP entrantes.
     *
     * Choix structurants :
     * - CSRF desactive : pertinent uniquement pour les sessions basees sur
     *   cookies ; ici l'authentification repose sur un jeton JWT envoye
     *   dans l'en-tete Authorization, donc la protection CSRF n'apporte
     *   rien et compliquerait inutilement les appels API.
     * - Session STATELESS : aucune session HTTP n'est creee ni utilisee
     *   cote serveur ; chaque requete doit se suffire a elle-meme (jeton
     *   JWT), coherent avec l'architecture stateless du backend.
     * - Endpoints publics explicitement listes (whitelist) : le login
     *   (pour pouvoir s'authentifier), le healthcheck (utilise par le
     *   healthcheck Docker de T2, qui ne peut evidemment pas fournir de
     *   jeton), et la documentation Swagger/OpenAPI. Tout le reste exige
     *   une authentification par defaut (anyRequest().authenticated()) :
     *   principe du moindre privilege, plutot que d'autoriser par defaut
     *   et de restreindre au cas par cas.
     * - Le filtre JWT (jwtFilter) est insere avant
     *   UsernamePasswordAuthenticationFilter, le filtre standard de Spring
     *   Security : cela garantit que le jeton est verifie et le contexte
     *   de securite peuple avant que Spring Security n'applique ses
     *   propres mecanismes d'authentification.
     *
     * @param http le constructeur de configuration de securite fourni par Spring
     * @return la chaine de filtres construite, enregistree comme bean
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)   // API stateless : pas de cookie de session
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/v1/auth/login",
                    "/actuator/health",
                    "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Fournit l'algorithme d'encodage des mots de passe utilise lors de la
     * creation des comptes utilisateurs (RG14) et de la verification au
     * login.
     *
     * BCrypt : algorithme adaptatif avec sel integre, standard de facto
     * pour le hachage de mots de passe (resiste aux attaques par table
     * arc-en-ciel et peut etre ralenti volontairement pour compliquer le
     * brute-force).
     *
     * @return l'encodeur de mots de passe, enregistre comme bean partage
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expose l'AuthenticationManager de Spring Security comme bean
     * injectable.
     *
     * Necessaire pour que le controleur d'authentification (endpoint de
     * login) puisse declencher explicitement la verification des
     * identifiants (login + mot de passe) fournis par l'utilisateur, avant
     * de generer le jeton JWT correspondant via JwtService.
     *
     * @param cfg la configuration d'authentification assemblee par Spring
     *            Security a partir des beans presents dans le contexte
     *            (notamment CustomUserDetailsService et PasswordEncoder)
     * @return l'AuthenticationManager pret a etre utilise pour verifier des
     *         identifiants
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
