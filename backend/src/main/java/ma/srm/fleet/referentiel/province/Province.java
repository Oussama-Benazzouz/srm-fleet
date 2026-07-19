package ma.srm.fleet.referentiel.province;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.srm.fleet.common.audit.AuditableEntity;

@Entity
@Table(name = "province")
@Getter @Setter @NoArgsConstructor
public class Province extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_province")
    private Long id;

    @Column(name = "libelle_province", nullable = false, unique = true, length = 100)
    private String libelle;
}