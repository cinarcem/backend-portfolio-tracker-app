package com.portfoliotracker.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String[] AUTH_WHITELIST = {
            //Sample data endpoints
            "/watchlist/api/v1/indexes/sample",
            "/watchlist/api/v1/stocks/sample",
            // Swagger endpoints
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/webjars/**",
            // Actuator endpoints
            "/actuator/health",
            "/actuator/info",
    };

    private static final String[] PROTECTED_PATHS = {
            "/portfolio/api/**",
            "/market-data/api/**",
            "/watchlist/api/**"
    };

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity){
        serverHttpSecurity
                .cors(withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange( exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(AUTH_WHITELIST).permitAll()
                        .pathMatchers(PROTECTED_PATHS).authenticated()
                        .anyExchange().permitAll()
                )
                .oauth2ResourceServer( oath2 -> oath2.jwt(withDefaults()) );

        return serverHttpSecurity.build();
    }

}
