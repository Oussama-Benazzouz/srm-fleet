package ma.srm.fleet.conducteur.permis;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.srm.fleet.common.audit.AuditableEntity;
import ma.srm.fleet.conducteur.conducteur.Conducteur;

@Entity
@Table(name = "permis")
@Getter @Setter @NoArgsConstructor
public class Permis extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permis")
    private Long id;

    /** VARCHAR libre : nouveaux types de permis sans refonte (exigence d'evolutivite). */
    @Column(name = "type_permis", nullable = false, length = 10)
    private String typePermis;

    @Column(name = "date_fin_validite_permis", nullable = false)
    private LocalDate dateFinValidite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conducteur", nullable = false)
    private Conducteur conducteur;
}