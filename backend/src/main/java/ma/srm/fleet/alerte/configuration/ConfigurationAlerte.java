package ma.srm.fleet.alerte.configuration;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.srm.fleet.common.audit.AuditableEntity;

@Entity
@Table(name = "configuration_alerte")
@Getter @Setter @NoArgsConstructor
public class ConfigurationAlerte extends AuditableEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "type_source_alerte", length = 30)
    private TypeSourceAlerte typeSource;

    @Column(name = "seuil_jours_alerte", nullable = false)
    private Integer seuilJours;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_defaut_alerte", nullable = false, length = 20)
    private CanalAlerte canalDefaut;
}