package ma.srm.fleet.referentiel.departement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.srm.fleet.common.audit.AuditableEntity;

@Entity
@Table(name = "departement")
@Getter @Setter @NoArgsConstructor
public class Departement extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departement")
    private Long id;

    @Column(name = "libelle_departement", nullable = false, unique = true, length = 100)
    private String libelle;
}