package ma.srm.fleet.alerte.configuration;

import java.util.Optional;
import ma.srm.fleet.alerte.configuration.dto.ConfigurationAlerteResponse;
import ma.srm.fleet.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationAlerteServiceTest {

    @Mock ConfigurationAlerteRepository repository;
    @InjectMocks ConfigurationAlerteService service;

    private ConfigurationAlerte config(TypeSourceAlerte type, int seuil) {
        ConfigurationAlerte c = new ConfigurationAlerte();
        c.setTypeSource(type);
        c.setSeuilJours(seuil);
        c.setCanalDefaut(CanalAlerte.TABLEAU_DE_BORD);
        return c;
    }

    @Test
    void modifierSeuil_ok() {
        when(repository.findById(TypeSourceAlerte.ASSURANCE))
            .thenReturn(Optional.of(config(TypeSourceAlerte.ASSURANCE, 30)));

        ConfigurationAlerteResponse res = service.modifierSeuil(TypeSourceAlerte.ASSURANCE, 45);

        assertThat(res.seuilJours()).isEqualTo(45);
        assertThat(res.typeSource()).isEqualTo(TypeSourceAlerte.ASSURANCE);
    }

    @Test
    void modifierSeuil_typeInconnu_leve404() {
        when(repository.findById(TypeSourceAlerte.PLAFOND_CARTE)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.modifierSeuil(TypeSourceAlerte.PLAFOND_CARTE, 10))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
