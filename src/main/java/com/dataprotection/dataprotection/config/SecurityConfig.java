package com.dataprotection.dataprotection.config;

import com.dataprotection.dataprotection.filter.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.dataprotection.dataprotection.service.AppUserDetailsService;

import java.util.List;
import java.util.Map;

/**
 * STEP 1+2 — SECURITY CONFIG WITH JWT + ROLES
 *
 * Replace your existing SecurityConfig.java with this file.
 * Place at: src/main/java/com/dataprotection/dataprotection/config/SecurityConfig.java
 *
 * Key changes from previous version:
 *  - HTTP Basic removed → JWT filter added
 *  - @EnableMethodSecurity added → enables @PreAuthorize on controller methods
 *  - Admin-only routes configured
 *  - WebSocket endpoint permitted
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize("hasRole('ADMIN')") on methods
public class SecurityConfig {

    private final AppUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(AppUserDetailsService userDetailsService,
                          JwtAuthFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                // STATELESS — no HttpSession. JWT carries all auth state.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Public endpoints — no token needed
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/api/files/public/**").permitAll()
                        .requestMatchers("/").permitAll()

                        // WebSocket endpoint — permitted, then secured at handler level
                        .requestMatchers("/ws/**").permitAll()

                        // Admin-only endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Everything else needs a valid JWT
                        .anyRequest().authenticated()
                )

                // Custom 401 response — JSON instead of browser popup
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthEntryPoint()))

                // JWT filter runs BEFORE Spring's UsernamePasswordAuthenticationFilter
                // so the SecurityContext is populated before any authorization checks
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint customAuthEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    new ObjectMapper().writeValueAsString(
                            Map.of("status", 401, "error", "Unauthorized",
                                    "message", "Token missing or expired. Please log in again.")
                    )
            );
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}