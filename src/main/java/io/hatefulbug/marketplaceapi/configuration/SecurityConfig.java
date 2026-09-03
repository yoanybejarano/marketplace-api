package io.hatefulbug.marketplaceapi.configuration;

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import io.hatefulbug.marketplaceapi.auth.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // Remove field and constructor entirely

    @Bean
    @SuppressWarnings("java:S4502") // Disabling CSRF is safe for stateless REST APIs using JWTs
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthFilter) throws Exception {

        return http
                // 1. Disable CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Enforce stateless session management
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Define authorization rules
                .authorizeHttpRequests(auth -> auth
                        // --- Actuator & Monitoring Endpoints ---
                        .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                        .requestMatchers(EndpointRequest.to("prometheus")).permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")

                        // --- Public API Endpoints ---
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                        // --- Role-Based Path Rules ---
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/staff/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/customers/**").hasRole("ADMIN")

                        // --- Default Fallback ---
                        .anyRequest().authenticated()
                )

                // 4. Register JWT authentication filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
