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

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity){
        serverHttpSecurity
                .cors(withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange( exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        .pathMatchers("/portfolio/api/**","/market-data/api/**","/watchlist/api/v1")
                        .authenticated()
                        .anyExchange()
                        .permitAll()
                )
                .oauth2ResourceServer( oath2 -> oath2.jwt(withDefaults()) );

        return serverHttpSecurity.build();
    }

}
