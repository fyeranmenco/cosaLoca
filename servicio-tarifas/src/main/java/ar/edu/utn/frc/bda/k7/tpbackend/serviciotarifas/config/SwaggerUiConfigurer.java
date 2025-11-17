package ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.config;

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

         props.setConfigUrl("/api/tarifas/v3/api-docs/swagger-config");

         SwaggerUrl tarifasSpec = new SwaggerUrl(
                "camiones",                                 
                "/api/tarifas/v3/api-docs",                
                "/api/tarifas/v3/api-docs/swagger-config" 
        );
        props.setUrls(Set.of(tarifasSpec));

         props.setOauth2RedirectUrl("http://localhost:8080/api/tarifas/swagger-ui/oauth2-redirect.html");

         
    }
}