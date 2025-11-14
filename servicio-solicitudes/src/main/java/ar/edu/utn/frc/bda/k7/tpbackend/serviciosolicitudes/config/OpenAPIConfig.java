package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    // 1. Define el nombre del "Security Scheme"
    private static final String SECURITY_SCHEME_NAME = "keycloak_oauth2";
    
    // 2. Define las URLs PÚBLICAS (a través del Gateway) de tu Keycloak
    // Esto usa la ruta que ya definiste en tu application.yml del Gateway
    private static final String AUTH_SERVER_URL = "http://localhost:8181/realms/tpbackend/protocol/openid-connect";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API de Logística (TPI)")
                .description("Documentación del microservicio.")
                .version("v1.0"))
            
			.servers(List.of(new Server().url("http://localhost:8080")))
            
            .components(
                new Components()
                    .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.OAUTH2)
                        .flows(new OAuthFlows()
                            .authorizationCode(new OAuthFlow()
                                // URL para la página de login de Keycloak
                                .authorizationUrl(AUTH_SERVER_URL + "/auth")
                                // URL para intercambiar el código por un token
                                .tokenUrl(AUTH_SERVER_URL + "/token")
                                .scopes(null) // Puedes dejar los scopes vacíos
                            )
                        )
                    )
            )

            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}