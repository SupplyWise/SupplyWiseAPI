package com.supplywise.supplywise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**").permitAll() //Allow access to Swagger UI
                .requestMatchers("/api/auth/**").permitAll() // Expose public part of the API
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasAnyRole("ADMIN", "FRANCHISE_OWNER", "MANAGER_MASTER", "MANAGER", "DISASSOCIATED")
                .requestMatchers("/api/company/**").hasAnyRole("ADMIN", "FRANCHISE_OWNER", "MANAGER_MASTER", "MANAGER", "DISASSOCIATED")
                .requestMatchers(HttpMethod.POST, "/api/company").hasAnyRole("ADMIN", "FRANCHISE_OWNER", "DISASSOCIATED") // Allow creating a company for disassociated users
                .requestMatchers("/api/company/**").hasAnyRole("ADMIN", "FRANCHISE_OWNER") // Restrict other company-related endpoints
                .requestMatchers("/api/item-properties/**").hasAnyRole("ADMIN", "FRANCHISE_OWNER", "MANAGER_MASTER", "MANAGER")
                .requestMatchers("/api/item/**").hasAnyRole("ADMIN", "FRANCHISE_OWNER", "MANAGER_MASTER", "MANAGER")
                .anyRequest().authenticated()
            )
            .exceptionHandling(handler -> handler
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write(accessDeniedException.getMessage());
                })
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);    
        return http.build();
    }
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Collections.singletonList("*")); //TODO this should be testing only, RESTRICT LATER
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("Authorization","Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**",configuration);

        return source;
    }
}
