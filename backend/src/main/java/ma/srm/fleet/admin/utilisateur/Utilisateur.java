package ma.srm.fleet.admin.utilisateur;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.srm.fleet.common.audit.AuditableEntity;
import ma.srm.fleet.referentiel.province.Province;

@Entity
@Table(name = "utilisateur")
@Getter @Setter @NoArgsConstructor
public class Utilisateur extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Long id;

    @Column(name = "nom_utilisateur", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom_utilisateur", nullable = false, length = 100)
    private String prenom;

    @Column(name = "login_utilisateur", nullable = false, unique = true, length = 100)
    private String login;

    @Column(name = "email_utilisateur", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "mot_de_passe_utilisateur", nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_utilisateur", nullable = false, length = 30)
    private RoleUtilisateur role;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_compte_utilisateur", nullable = false, length = 20)
    private StatutCompte statutCompte;

    /** NULL si administrateur central (RG14). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_province")
    private Province province;
}