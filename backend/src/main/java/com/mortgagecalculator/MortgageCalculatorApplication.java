package com.mortgagecalculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableConfigurationProperties
public class MortgageCalculatorApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(MortgageCalculatorApplication.class, args);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Get allowed origins from environment or use defaults
        String allowedOrigins = System.getenv("ALLOWED_ORIGINS");
        String[] origins;

        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            origins = allowedOrigins.split(",");
            System.out.println("Using CORS origins from environment: " + allowedOrigins);
        } else {
            // Default origins for local development and Azure
            origins = new String[] {
                    "http://localhost:5173",
                    "http://localhost:3000",
                    "http://localhost:8081",
                    "https://frontend.grayisland-6acd1730.eastus2.azurecontainerapps.io",
                    "*" // Temporary - allow all origins for testing
            };
            System.out.println("Using default CORS origins including wildcard");
        }

        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false); // Set to false for Azure Container Apps
    }
}