package ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.config;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

import java.util.Set;

/**
 * Configura Swagger UI en tiempo de arranque (springdoc 2.x).
 * - Fuerza configUrl
 * - Define las specs disponibles con SwaggerUrl
 * - Ajusta oauth2RedirectUrl para que coincida con la registrada en Keycloak
 */
@Configuration
public class SwaggerUiConfigurer {

    private final SwaggerUiConfigProperties props;

    public SwaggerUiConfigurer(SwaggerUiConfigProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        System.out.println("\n\n\n>>> SwaggerUiConfigurer.init() ejecutándose con SwaggerUiConfigProperties\n\n\n");

        // 1) Forzar configUrl (ruta relativa vía gateway)
        props.setConfigUrl("/api/depositos/v3/api-docs/swagger-config");

        // 2) Registrar la spec pública usando SwaggerUrl
        SwaggerUrl clientesSpec = new SwaggerUrl(
                "depositos",                                // nombre visible
                "/api/depositos/v3/api-docs",               // URL de la spec
                "/api/depositos/v3/api-docs/swagger-config" // URL del swagger-config
        );
        props.setUrls(Set.of(clientesSpec));

        // 3) Forzar la redirect pública
        props.setOauth2RedirectUrl("http://localhost:8080/api/depositos/swagger-ui/oauth2-redirect.html");

        // Si preferís absoluta:
        // props.setOauth2RedirectUrl("http://localhost:8080/api/clientes/swagger-ui/oauth2-redirect.html");
    }
}