package ma.srm.fleet.carte.cartecarburant;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.srm.fleet.affectation.Affectation;
import ma.srm.fleet.carte.demande.DemandeCarteCarburant;
import ma.srm.fleet.common.audit.AuditableEntity;
import ma.srm.fleet.referentiel.fournisseur.Fournisseur;

@Entity
@Table(name = "carte_carburant")
@Getter @Setter @NoArgsConstructor
public class CarteCarburant extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carte")
    private Long id;

    @Column(name = "numero_carte", nullable = false, unique = true, length = 50)
    private String numeroCarte;

    /** RF-15 : strictement positif (CHECK en base). */
    @Column(name = "plafond_montant_carte", nullable = false, precision = 10, scale = 2)
    private BigDecimal plafondMontant;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_carte", nullable = false, length = 20)
    private StatutCarte statut;

    @Column(name = "date_creation_carte", nullable = false)
    private LocalDate dateCreation;

    /** RG4 : toujours rattachee a une affectation, jamais au vehicule seul. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_affectation", nullable = false)
    private Affectation affectation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_fournisseur", nullable = false)
    private Fournisseur fournisseur;

    /** RG5 : une carte nait toujours d'une demande validee. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_demande", nullable = false)
    private DemandeCarteCarburant demandeOrigine;
}
