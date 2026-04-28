package com.dataprotection.dataprotection.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for the React frontend.
 *
 * Place this file in:
 *   src/main/java/com/dataprotection/dataprotection/config/CorsConfig.java
 *
 * This allows the Vite dev server (port 5173) to make API calls to
 * Spring Boot (port 8080) with credentials (session cookies).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")   // Vite dev server
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)                    // Required for session cookies
                .maxAge(3600);
    }
}
