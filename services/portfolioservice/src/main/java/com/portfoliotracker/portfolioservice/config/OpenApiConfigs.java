package com.portfoliotracker.portfolioservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@OpenAPIDefinition
@Configuration
public class OpenApiConfigs {

    @Bean
    public OpenAPI departmentOpenAPI(
            @Value("${openapi.service.title:Default title}") String serviceTitle,
            @Value("${openapi.service.version:Default version}") String serviceVersion,
            @Value("${openapi.service.url:Default url}") String url,
            @Value("${openapi.service.description:Default Description}") String description,
            @Value("${openapi.service.contact.name:Default Contact Name}") String contactName,
            @Value("${openapi.service.contact.email:Default Contact Email}") String contactEmail,
            @Value("${openapi.service.contact.url:Default Contact Url}") String contactUrl) {
        return new OpenAPI()
                .servers(List.of(new Server().url(url)))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .info(new Info()
                        .title(serviceTitle)
                        .version(serviceVersion)
                        .description(description)
                        .contact(new Contact()
                                .name(contactName)
                                .email(contactEmail)
                                .url(contactUrl)
                        ));
    }
}
