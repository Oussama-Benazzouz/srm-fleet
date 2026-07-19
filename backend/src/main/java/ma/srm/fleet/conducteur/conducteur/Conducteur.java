package ma.srm.fleet.conducteur.conducteur;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.srm.fleet.common.audit.AuditableEntity;
import ma.srm.fleet.referentiel.departement.Departement;

@Entity
@Table(name = "conducteur")
@Getter @Setter @NoArgsConstructor
public class Conducteur extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conducteur")
    private Long id;

    @Column(name = "matricule_conducteur", nullable = false, unique = true, length = 50)
    private String matricule;

    @Column(name = "nom_conducteur", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom_conducteur", nullable = false, length = 100)
    private String prenom;

    @Column(name = "fonction_conducteur", length = 100)
    private String fonction;

    @Column(name = "groupe_sanguin_conducteur", length = 3)
    private String groupeSanguin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departement")
    private Departement departement;
}