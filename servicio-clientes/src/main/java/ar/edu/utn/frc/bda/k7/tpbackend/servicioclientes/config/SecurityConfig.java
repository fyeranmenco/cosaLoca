package ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
// Habilita las anotaciones @PreAuthorize y @PostAuthorize en los Controllers
@EnableMethodSecurity(prePostEnabled = true) 
public class SecurityConfig {

    /**
     * Define la cadena de filtros de seguridad para las peticiones HTTP.
    //  */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Deshabilita CSRF: Las APIs REST son stateless, no necesitan esta protección.
            .csrf(AbstractHttpConfigurer::disable)
            
            .authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/**", "/registrarme").permitAll() 
    
				.anyRequest().authenticated()
			)
            
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

	@Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            
            Object realmAccess = jwt.getClaims().get("realm_access");
            if (realmAccess instanceof Map<?, ?> realmAccessMap) {
                Object roles = realmAccessMap.get("roles");
                if (roles instanceof List<?> rolesList) {
                    return rolesList.stream()
                        .filter(String.class::isInstance)
                        .map(role -> "ROLE_" + ((String) role).toUpperCase()) 
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                }
            }
            return grantedAuthoritiesConverter.convert(jwt); // Devuelve solo los roles estándar si no encuentra los de Keycloak
        });
        
        return jwtAuthenticationConverter;
    }
}