package ma.srm.fleet.referentiel.fournisseur;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.srm.fleet.common.audit.AuditableEntity;

@Entity
@Table(name = "fournisseur")
@Getter @Setter @NoArgsConstructor
public class Fournisseur extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fournisseur")
    private Long id;

    @Column(name = "raison_sociale_fournisseur", nullable = false, length = 150)
    private String raisonSociale;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_fournisseur", nullable = false, length = 30)
    private TypeFournisseur type;

    @Column(name = "contact_fournisseur", length = 150)
    private String contact;

    @Column(name = "adresse_facturation_fournisseur", length = 255)
    private String adresseFacturation;
}
