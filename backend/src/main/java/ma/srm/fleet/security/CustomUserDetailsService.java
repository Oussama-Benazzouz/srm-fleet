package ma.srm.fleet.security;

import ma.srm.fleet.admin.utilisateur.UtilisateurRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation Spring Security de UserDetailsService, specifique au
 * modele de donnees du projet.
 *
 * C'est le point d'entree que Spring Security appelle automatiquement lors
 * de chaque tentative d'authentification (login) : il lui fournit
 * uniquement le login saisi, et attend en retour un UserDetails complet.
 * Cette classe fait le lien entre ce contrat Spring Security et le
 * UtilisateurRepository (couche persistance) du projet.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository repository;

    // Injection par constructeur (convention du projet, jamais par champ)
    public CustomUserDetailsService(UtilisateurRepository repository) {
        this.repository = repository;
    }

    /**
     * Charge un utilisateur par son login et le convertit en UserDetails.
     *
     * @Transactional(readOnly = true) : necessaire ici a cause de la
     * relation LAZY entre Utilisateur et Province (voir l'entite Vehicule
     * et le mapping @ManyToOne(fetch = FetchType.LAZY) du meme type sur
     * Utilisateur). Sans transaction active au moment de la construction
     * de UserPrincipal, l'appel a u.getProvince().getId() dans le
     * constructeur echouerait avec une LazyInitializationException, la
     * session Hibernate etant deja fermee.
     *
     * @param login l'identifiant de connexion saisi par l'utilisateur
     * @return le UserDetails correspondant, pret a etre verifie par
     *         Spring Security (mot de passe, statut du compte, roles)
     * @throws UsernameNotFoundException si aucun utilisateur ne correspond
     *         a ce login ; message volontairement generique cote
     *         utilisateur final pour ne pas reveler si le compte existe
     */
    @Override
    @Transactional(readOnly = true)   // pour charger la province (LAZY) dans la session
    public UserDetails loadUserByUsername(String login) {
        return repository.findByLogin(login)
            .map(UserPrincipal::new)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + login));
    }
}
