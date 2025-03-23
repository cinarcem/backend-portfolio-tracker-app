package com.portfoliotracker.portfolioservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = {SystemMetricsAutoConfiguration.class})
@EnableDiscoveryClient
public class PortfolioserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfolioserviceApplication.class, args);
	}

}
