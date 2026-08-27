package ma.srm.fleet.config;

import java.util.Optional;
import ma.srm.fleet.admin.utilisateur.Utilisateur;
import ma.srm.fleet.admin.utilisateur.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPasswordInitializerTest {

    private static final String PLACEHOLDER = "A_DEFINIR_PHASE_3";

    @Mock UtilisateurRepository repository;
    @Mock PasswordEncoder encoder;

    private Utilisateur admin(String motDePasse) {
        Utilisateur u = new Utilisateur();
        u.setLogin("admin");
        u.setMotDePasse(motDePasse);
        return u;
    }

    @Test
    void initialise_le_mot_de_passe_quand_configure_et_placeholder() {
        Utilisateur admin = admin(PLACEHOLDER);
        when(repository.findByLogin("admin")).thenReturn(Optional.of(admin));
        when(encoder.encode("Str0ng!Pass")).thenReturn("HASH");
        var init = new AdminPasswordInitializer(repository, encoder, "Str0ng!Pass");

        init.run(null);

        assertThat(admin.getMotDePasse()).isEqualTo("HASH");
        verify(repository).save(admin);
    }

    @Test
    void n_applique_aucun_defaut_si_mot_de_passe_absent() {
        Utilisateur admin = admin(PLACEHOLDER);
        when(repository.findByLogin("admin")).thenReturn(Optional.of(admin));
        var init = new AdminPasswordInitializer(repository, encoder, "");

        init.run(null);

        assertThat(admin.getMotDePasse()).isEqualTo(PLACEHOLDER); // inchangé → compte inutilisable
        verify(repository, never()).save(any());
    }

    @Test
    void ne_retouche_pas_un_compte_deja_initialise() {
        when(repository.findByLogin("admin")).thenReturn(Optional.of(admin("$2a$deja-hashe")));
        lenient().when(encoder.encode(any())).thenReturn("HASH");
        var init = new AdminPasswordInitializer(repository, encoder, "Str0ng!Pass");

        init.run(null);

        verify(repository, never()).save(any());
    }
}
