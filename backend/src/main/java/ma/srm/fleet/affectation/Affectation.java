package ma.srm.fleet.affectation;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.srm.fleet.common.audit.AuditableEntity;
import ma.srm.fleet.conducteur.conducteur.Conducteur;
import ma.srm.fleet.referentiel.vehicule.Vehicule;

@Entity
@Table(name = "affectation")
@Getter @Setter @NoArgsConstructor
public class Affectation extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_affectation")
    private Long id;

    @Column(name = "date_debut_affectation", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin_affectation")
    private LocalDate dateFin;

    @Column(name = "motif_affectation", length = 255)
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_affectation", nullable = false, length = 20)
    private StatutAffectation statut;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vehicule", nullable = false)
    private Vehicule vehicule;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conducteur", nullable = false)
    private Conducteur conducteur;
}
