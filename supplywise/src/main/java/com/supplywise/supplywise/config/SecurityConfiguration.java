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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    private static final String ADMIN = "ADMIN";
    private static final String FRANCHISE_OWNER = "FRANCHISE_OWNER";
    private static final String MANAGER_MASTER = "MANAGER_MASTER";
    private static final String MANAGER = "MANAGER";
    private static final String DISASSOCIATED = "DISASSOCIATED";

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
                .requestMatchers("/api/auth/**", "/api/health").permitAll() // Expose public part of the API
                .requestMatchers("/api/admin/**").hasRole(ADMIN)
                .requestMatchers("/api/company/**").hasAnyRole(ADMIN, FRANCHISE_OWNER, MANAGER_MASTER, MANAGER, DISASSOCIATED)
                .requestMatchers(HttpMethod.POST, "/api/company").hasAnyRole(ADMIN, FRANCHISE_OWNER, DISASSOCIATED) // Allow creating a company for disassociated users
                .requestMatchers("/api/company/**").hasAnyRole(ADMIN, FRANCHISE_OWNER) // Restrict other company-related endpoints
                .requestMatchers("/api/inventories/**").hasAnyRole(ADMIN, FRANCHISE_OWNER, MANAGER_MASTER, MANAGER)
                .requestMatchers("/api/item/**").hasAnyRole(ADMIN, FRANCHISE_OWNER, MANAGER_MASTER, MANAGER)
                .requestMatchers("/api/item-properties/**").hasAnyRole(ADMIN, FRANCHISE_OWNER, MANAGER_MASTER, MANAGER)
                .requestMatchers("/api/notification/**").hasAnyRole(ADMIN, FRANCHISE_OWNER, MANAGER_MASTER, MANAGER)
                .requestMatchers("/api/restaurants/**").hasAnyRole(ADMIN, FRANCHISE_OWNER, MANAGER_MASTER, MANAGER)
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

        configuration.addAllowedOriginPattern("*");
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("Authorization","Content-Type", "X-Requested-With", "Accept", "Access-Control-Allow-Origin"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",configuration);

        return source;
    }
}
