package ma.srm.fleet.conformite.assurance;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.srm.fleet.common.audit.AuditableEntity;
import ma.srm.fleet.referentiel.vehicule.Vehicule;

@Entity
@Table(name = "assurance")
@Getter @Setter @NoArgsConstructor
public class Assurance extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_assurance")
    private Long id;

    @Column(name = "nom_compagnie_assurance", nullable = false, length = 150)
    private String nomCompagnie;

    @Column(name = "date_debut_assurance", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_echeance_assurance", nullable = false)
    private LocalDate dateEcheance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vehicule", nullable = false)
    private Vehicule vehicule;
}