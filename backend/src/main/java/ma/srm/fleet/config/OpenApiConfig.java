package ma.srm.fleet.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de la documentation OpenAPI de l'application.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fleetOpenAPI(){
        return new OpenAPI()
            .info(new Info()
                .title("SRM Fleet API")
                .description("Backoffice de gestion de la flotte et de la logistique — SRM Souss-Massa")
                .version("v1"));
    }
}
