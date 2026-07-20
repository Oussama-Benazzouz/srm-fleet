package ma.srm.fleet.carte.demande;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.srm.fleet.admin.utilisateur.Utilisateur;
import ma.srm.fleet.affectation.Affectation;
import ma.srm.fleet.common.audit.AuditableEntity;

@Entity
@Table(name = "demande_carte_carburant")
@Getter @Setter @NoArgsConstructor
public class DemandeCarteCarburant extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande")
    private Long id;

    @Column(name = "date_demande_carte", nullable = false)
    private LocalDate dateDemande;

    @Column(name = "motif_demande_carte", length = 255)
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_demande_carte", nullable = false, length = 20)
    private StatutDemandeCarte statut;

    @Column(name = "date_traitement_demande_carte")
    private LocalDate dateTraitement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_affectation", nullable = false)
    private Affectation affectation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_utilisateur_demandeur", nullable = false)
    private Utilisateur demandeur;

    /** NULL tant que la demande n'est pas traitee. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur_validateur")
    private Utilisateur validateur;
}
