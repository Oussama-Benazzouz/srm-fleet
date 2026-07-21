package ma.srm.fleet.auth;

import jakarta.validation.Valid;
import ma.srm.fleet.auth.dto.LoginRequest;
import ma.srm.fleet.auth.dto.LoginResponse;
import ma.srm.fleet.security.UserPrincipal;
import ma.srm.fleet.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controleur exposant les deux endpoints d'authentification du module
 * security : connexion (emission d'un jeton) et consultation du profil
 * courant (verification qu'un jeton deja emis est toujours valide).
 *
 * Rappel de securite (SecurityConfig) : seul /login est public
 * (permitAll()) ; /me exige un jeton valide, comme tout autre endpoint de
 * l'application.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // Injection par constructeur (convention du projet)
    public AuthController(AuthenticationManager am, JwtService js) {
        this.authenticationManager = am;
        this.jwtService = js;
    }

    /**
     * Verifie les identifiants fournis et emet un jeton JWT en cas de succes.
     *
     * authenticationManager.authenticate(...) declenche en interne :
     * CustomUserDetailsService (chargement de l'utilisateur), puis
     * comparaison du mot de passe fourni avec le hash BCrypt stocke
     * (PasswordEncoder). En cas d'echec, une AuthenticationException est
     * levee automatiquement et interceptee par SecurityExceptionHandler
     * (reponse 401 normalisee) — aucune gestion d'erreur manuelle
     * necessaire ici.
     *
     * @param request identifiants saisis par l'utilisateur
     * @return le jeton JWT et les informations de profil de l'utilisateur
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.login(), request.motDePasse()));
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(user);
        return new LoginResponse(token, user.getUsername(), user.getRole(), user.getProvinceId());
    }

    /**
     * Retourne le profil de l'utilisateur actuellement authentifie, a
     * partir du jeton deja fourni dans l'en-tete Authorization de la
     * requete.
     *
     * @AuthenticationPrincipal : injecte directement le UserPrincipal deja
     * place dans le SecurityContext par JwtAuthenticationFilter — aucune
     * recherche supplementaire en base n'est necessaire ici, contrairement
     * a /login qui doit verifier un mot de passe.
     *
     * Utilise typiquement par le frontend au demarrage de l'application
     * pour restaurer une session a partir d'un jeton deja stocke
     * localement (localStorage/sessionStorage cote Angular), et confirmer
     * qu'il est toujours valide.
     *
     * @param user le principal courant, injecte automatiquement par Spring
     *             Security a partir du jeton de la requete
     * @return les informations de profil, sans nouveau jeton (voir remarque)
     */
    @GetMapping("/me")
    public LoginResponse me(@AuthenticationPrincipal UserPrincipal user) {
        return new LoginResponse(null, user.getUsername(), user.getRole(), user.getProvinceId());
    }
}
