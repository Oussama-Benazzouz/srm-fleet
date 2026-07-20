package ma.srm.fleet.conformite.sinistre;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.srm.fleet.affectation.Affectation;
import ma.srm.fleet.common.audit.AuditableEntity;
import ma.srm.fleet.referentiel.vehicule.Vehicule;

@Entity
@Table(name = "sinistre")
@Getter @Setter @NoArgsConstructor
public class Sinistre extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sinistre")
    private Long id;

    @Column(name = "date_sinistre", nullable = false)
    private LocalDate dateSinistre;

    @Column(name = "description_sinistre", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_sinistre", nullable = false, length = 20)
    private StatutSinistre statut;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vehicule", nullable = false)
    private Vehicule vehicule;

    /** NULL si aucune affectation active a la date des faits (RG7). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_affectation")
    private Affectation affectation;
}