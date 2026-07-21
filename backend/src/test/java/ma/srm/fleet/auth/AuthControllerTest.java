package ma.srm.fleet.auth;

import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.admin.utilisateur.StatutCompte;
import ma.srm.fleet.admin.utilisateur.Utilisateur;
import ma.srm.fleet.common.error.GlobalExceptionHandler;
import ma.srm.fleet.security.CustomUserDetailsService;
import ma.srm.fleet.security.SecurityExceptionHandler;
import ma.srm.fleet.security.UserPrincipal;
import ma.srm.fleet.security.jwt.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests du contrôleur d'authentification.
 *
 * Sécurité MVC désactivée (addFilters = false) : on teste ici la logique
 * du contrôleur et des ExceptionHandlers, pas la chaîne de filtres Spring
 * Security elle-même. Pour /me, comme SecurityContextHolderFilter ne
 * s'exécute pas (filtres désactivés), .with(authentication(...)) ne suffit
 * pas à peupler @AuthenticationPrincipal : on positionne directement
 * SecurityContextHolder avant l'appel, ce qui fonctionne car MockMvc
 * s'exécute de façon synchrone sur le thread du test.
 *
 * CustomUserDetailsService est mocké même si addFilters = false : @WebMvcTest
 * instancie quand même les beans Filter présents dans le contexte (dont
 * JwtAuthenticationFilter), qui a besoin de ce bean pour être construit.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, SecurityExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private UserPrincipal buildAdminPrincipal() {
        Utilisateur u = mock(Utilisateur.class);
        when(u.getId()).thenReturn(1L);
        when(u.getLogin()).thenReturn("admin");
        when(u.getMotDePasse()).thenReturn("hashed");
        when(u.getRole()).thenReturn(RoleUtilisateur.ADMINISTRATEUR_CENTRAL);
        when(u.getProvince()).thenReturn(null);
        when(u.getStatutCompte()).thenReturn(StatutCompte.ACTIF);
        return new UserPrincipal(u);
    }

    @Test
    void login_avecIdentifiantsValides_renvoie200EtToken() throws Exception {
        UserPrincipal admin = buildAdminPrincipal();
        Authentication auth = new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken(any())).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"login":"admin","motDePasse":"Admin@123"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("fake-jwt-token"))
            .andExpect(jsonPath("$.login").value("admin"))
            .andExpect(jsonPath("$.role").value("ADMINISTRATEUR_CENTRAL"));
    }

    @Test
    void login_avecMauvaisMotDePasse_renvoie401AvecProblemDetail() throws Exception {
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"login":"admin","motDePasse":"faux"}
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.title").value("Authentification échouée"))
            .andExpect(jsonPath("$.detail").value("Identifiants invalides ou compte désactivé"));
    }

    @Test
    void login_avecJsonMalforme_renvoie500() throws Exception {
        // Corps volontairement tronqué : Jackson doit échouer au parsing
        // (JsonEOFException) quels que soient les réglages de tolérance,
        // contrairement à un simple guillemet manquant qui peut être
        // interprété comme un nom de champ non quoté et passer la
        // désérialisation (→ 400 de validation, pas 500).
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.title").value("Erreur interne"));
    }

    @Test
    void me_avecUtilisateurAuthentifie_renvoie200() throws Exception {
        UserPrincipal admin = buildAdminPrincipal();
        Authentication auth = new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/api/v1/auth/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").doesNotExist())
            .andExpect(jsonPath("$.login").value("admin"))
            .andExpect(jsonPath("$.role").value("ADMINISTRATEUR_CENTRAL"));
    }
}
