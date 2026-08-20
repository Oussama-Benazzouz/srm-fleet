package ma.srm.fleet.alerte.configuration;

import org.springframework.data.jpa.repository.JpaRepository;

/** La clé primaire est le type de source (enum persisté en VARCHAR). */
public interface ConfigurationAlerteRepository
    extends JpaRepository<ConfigurationAlerte, TypeSourceAlerte> {
}
