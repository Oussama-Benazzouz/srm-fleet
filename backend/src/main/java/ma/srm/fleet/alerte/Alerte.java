package ma.srm.fleet.alerte;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.srm.fleet.alerte.configuration.CanalAlerte;
import ma.srm.fleet.alerte.configuration.ConfigurationAlerte;
import ma.srm.fleet.common.audit.AuditableEntity;

@Entity
@Table(name = "alerte")
@Getter @Setter @NoArgsConstructor
public class Alerte extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerte")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_source_alerte", nullable = false)
    private ConfigurationAlerte configuration;

    /** Id de l'objet source (assurance, visite ou carte) selon le type. */
    @Column(name = "id_reference_alerte", nullable = false)
    private Long idReference;

    @Column(name = "date_declenchement_alerte", nullable = false)
    private LocalDateTime dateDeclenchement;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_alerte", nullable = false, length = 20)
    private CanalAlerte canal;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_lecture_alerte", nullable = false, length = 20)
    private StatutLecture statutLecture;
}