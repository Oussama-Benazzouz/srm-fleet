package ma.srm.fleet.alerte.engine;

import java.time.LocalDate;
import java.util.List;
import ma.srm.fleet.alerte.Alerte;
import ma.srm.fleet.alerte.AlerteRepository;
import ma.srm.fleet.alerte.configuration.CanalAlerte;
import ma.srm.fleet.alerte.configuration.ConfigurationAlerte;
import ma.srm.fleet.alerte.configuration.ConfigurationAlerteRepository;
import ma.srm.fleet.alerte.configuration.TypeSourceAlerte;
import ma.srm.fleet.conformite.assurance.Assurance;
import ma.srm.fleet.conformite.assurance.AssuranceRepository;
import ma.srm.fleet.conformite.visitetechnique.VisiteTechniqueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlerteGenerationServiceTest {

    @Mock ConfigurationAlerteRepository configRepository;
    @Mock AssuranceRepository assuranceRepository;
    @Mock VisiteTechniqueRepository visiteRepository;
    @Mock AlerteRepository alerteRepository;
    @InjectMocks AlerteGenerationService service;

    private ConfigurationAlerte configAssurance() {
        ConfigurationAlerte c = new ConfigurationAlerte();
        c.setTypeSource(TypeSourceAlerte.ASSURANCE);
        c.setSeuilJours(30);
        c.setCanalDefaut(CanalAlerte.TABLEAU_DE_BORD);
        return c;
    }

    private Assurance assurance(Long id) {
        Assurance a = new Assurance();
        a.setId(id);
        return a;
    }

    @Test
    void genere_alerte_pour_echeance_non_encore_signalee() {
        when(configRepository.findAll()).thenReturn(List.of(configAssurance()));
        when(assuranceRepository.findEcheancesAControler(any(LocalDate.class)))
            .thenReturn(List.of(assurance(5L)));
        when(alerteRepository.existsByConfiguration_TypeSourceAndIdReference(TypeSourceAlerte.ASSURANCE, 5L))
            .thenReturn(false);

        int crees = service.genererToutes();

        assertThat(crees).isEqualTo(1);
        verify(alerteRepository).save(any(Alerte.class));
    }

    @Test
    void ne_recree_pas_une_alerte_deja_existante() {
        when(configRepository.findAll()).thenReturn(List.of(configAssurance()));
        when(assuranceRepository.findEcheancesAControler(any(LocalDate.class)))
            .thenReturn(List.of(assurance(5L)));
        when(alerteRepository.existsByConfiguration_TypeSourceAndIdReference(TypeSourceAlerte.ASSURANCE, 5L))
            .thenReturn(true);

        int crees = service.genererToutes();

        assertThat(crees).isZero();
        verify(alerteRepository, never()).save(any(Alerte.class));
    }
}
