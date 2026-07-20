package ma.srm.fleet.conformite.visitetechnique;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.srm.fleet.common.audit.AuditableEntity;
import ma.srm.fleet.conducteur.conducteur.Conducteur;
import ma.srm.fleet.referentiel.vehicule.Vehicule;

@Entity
@Table(name = "visite_technique")
@Getter @Setter @NoArgsConstructor
public class VisiteTechnique extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_visite")
    private Long id;

    @Column(name = "date_visite", nullable = false)
    private LocalDate dateVisite;

    @Column(name = "date_echeance_visite", nullable = false)
    private LocalDate dateEcheance;

    @Column(name = "nom_compagnie_visite", nullable = false, length = 150)
    private String nomCompagnie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vehicule", nullable = false)
    private Vehicule vehicule;

    /** Conducteur accompagnateur (RG11) — controle metier en Phase 8. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conducteur", nullable = false)
    private Conducteur conducteurAccompagnateur;
}