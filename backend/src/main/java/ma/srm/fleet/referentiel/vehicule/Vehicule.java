package ma.srm.fleet.referentiel.vehicule;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.srm.fleet.common.audit.AuditableEntity;
import ma.srm.fleet.referentiel.departement.Departement;
import ma.srm.fleet.referentiel.fournisseur.Fournisseur;
import ma.srm.fleet.referentiel.province.Province;

@Entity
@Table(name = "vehicule")
@Getter @Setter @NoArgsConstructor
public class Vehicule extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehicule")
    private Long id;

    @Column(name = "immatriculation_vehicule", nullable = false, unique = true, length = 20)
    private String immatriculation;

    @Column(name = "type_vehicule", nullable = false, length = 50)
    private String typeVehicule;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_acquisition_vehicule", nullable = false, length = 20)
    private ModeAcquisition modeAcquisition;

    @Column(name = "date_mise_service_vehicule")
    private LocalDate dateMiseEnService;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_vehicule", nullable = false, length = 20)
    private StatutVehicule statut;

    @Column(name = "numero_contrat_vehicule", length = 50)
    private String numeroContrat;

    @Column(name = "date_debut_contrat_vehicule")
    private LocalDate dateDebutContrat;

    @Column(name = "date_fin_contrat_vehicule")
    private LocalDate dateFinContrat;

    @Column(name = "loyer_mensuel_vehicule", precision = 10, scale = 2)
    private BigDecimal loyerMensuel;

    @Column(name = "plafond_km_vehicule")
    private Integer plafondKm;

    /** RG1 : obligatoire. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_province", nullable = false)
    private Province province;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departement")
    private Departement departement;

    /** RG2 : obligatoire si location (regle applicative Phase 5). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fournisseur")
    private Fournisseur fournisseur;
}
