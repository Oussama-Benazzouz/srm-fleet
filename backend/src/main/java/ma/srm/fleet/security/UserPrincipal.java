package ma.srm.fleet.security;

import java.util.Collection;
import java.util.List;

import lombok.Getter;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.admin.utilisateur.StatutCompte;
import ma.srm.fleet.admin.utilisateur.Utilisateur;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Adaptateur entre l'entite Utilisateur (modele metier/JPA) et l'interface
 * UserDetails attendue par Spring Security.
 *
 * Spring Security ne connait rien du modele de donnees de l'application ; il
 * manipule uniquement des objets UserDetails. Cette classe fait donc le pont
 * en encapsulant une copie des champs necessaires a l'authentification et
 * a l'autorisation, sans exposer directement l'entite Utilisateur au
 * contexte de securite.
 *
 * Immuable (tous les champs sont final) : une fois construit a partir d'un
 * Utilisateur, le principal ne change plus pendant la duree de la requete
 * ou de la session.
 */
public class UserPrincipal implements UserDetails {

    // Accesseurs metier supplementaires, en plus du contrat UserDetails :
    // utilises par la couche de filtrage par province (RG14) et par les
    // controleurs qui ont besoin de l'identifiant reel de l'utilisateur.
    @Getter
    private final Long id;
    private final String login;
    private final String motDePasse;
    @Getter
    private final RoleUtilisateur role;
    @Getter
    private final Long provinceId;
    private final boolean actif;

    /**
     * Construit le principal a partir de l'entite Utilisateur chargee en base.
     *
     * @param u l'utilisateur authentifie, recupere via UtilisateurRepository
     */
    public UserPrincipal(Utilisateur u) {
        this.id = u.getId();
        this.login = u.getLogin();
        this.motDePasse = u.getMotDePasse();
        this.role = u.getRole();
        // provinceId reste null pour l'Administrateur Central (portee nationale,
        // pas de province associee) ; renseigne pour le Responsable Provincial.
        this.provinceId = (u.getProvince() != null) ? u.getProvince().getId() : null;
        // Traduit l'enum StatutCompte en booleen simple, directement exploitable
        // par isEnabled() ci-dessous.
        this.actif = u.getStatutCompte() == StatutCompte.ACTIF;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Le prefixe ROLE_ est la convention attendue par hasRole(...)
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // Methodes du contrat UserDetails, mappees sur les champs internes.

    @Override public String getPassword() { return motDePasse; }
    @Override public String getUsername() { return login; }

    // Ces trois indicateurs ne correspondent a aucune regle de gestion du
    // projet (pas d'expiration de compte, pas de verrouillage, pas
    // d'expiration de mot de passe prevue) : toujours vrais par defaut.
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    // Seul indicateur reellement significatif ici : un compte desactive
    // (statut != ACTIF) ne peut plus s'authentifier, quel que soit le mot
    // de passe fourni. Spring Security rejette la connexion automatiquement
    // si isEnabled() renvoie false.
    @Override public boolean isEnabled() { return actif; }   // RF-27 : compte désactivé = non authentifiable
}
