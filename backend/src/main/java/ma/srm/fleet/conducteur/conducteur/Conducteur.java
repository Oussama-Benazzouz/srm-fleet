package ma.srm.fleet.conducteur.conducteur;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.srm.fleet.common.audit.AuditableEntity;
import ma.srm.fleet.referentiel.departement.Departement;
import ma.srm.fleet.referentiel.province.Province;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_province", nullable = false)
    private Province province;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_departement", nullable = false)
    private Departement departement;

    @OneToMany(mappedBy = "conducteur", fetch = FetchType.LAZY,
        cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ma.srm.fleet.conducteur.permis.Permis> permis = new java.util.ArrayList<>();
}
