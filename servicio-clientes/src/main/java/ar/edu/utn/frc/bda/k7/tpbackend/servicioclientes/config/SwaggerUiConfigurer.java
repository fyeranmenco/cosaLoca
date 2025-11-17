package ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.config;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

import java.util.Set;


@Configuration
public class SwaggerUiConfigurer {

    private final SwaggerUiConfigProperties props;

    public SwaggerUiConfigurer(SwaggerUiConfigProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        System.out.println("\n\n\n>>> SwaggerUiConfigurer.init() ejecutándose con SwaggerUiConfigProperties\n\n\n");

        props.setConfigUrl("http://localhost:8080/api/clientes/v3/api-docs/swagger-config");

        SwaggerUrl clientesSpec = new SwaggerUrl(
                "clientes",                                
                "http://localhost:8080/api/clientes/v3/api-docs",               
                "http://localhost:8080/api/clientes/v3/api-docs/swagger-config" 
        );
        props.setUrls(Set.of(clientesSpec));

        props.setOauth2RedirectUrl("http://localhost:8080/api/clientes/swagger-ui/oauth2-redirect.html");
    }
}