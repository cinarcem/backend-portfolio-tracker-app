package com.portfoliotracker.gateway;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Bean
	public RouteLocator routeLocator(RouteLocatorBuilder builder) {
		return builder
				.routes()
				.route(r -> r.path("/test/v3/api-docs").and().method(HttpMethod.GET).uri("lb://test"))
				.route(r -> r.path("/log4jcontroller/v3/api-docs").and().method(HttpMethod.GET).uri("lb://logtest"))
				.route(r -> r.path("/market-data/v3/api-docs").and().method(HttpMethod.GET).uri("lb://marketdata"))
				.build();
	}
}
