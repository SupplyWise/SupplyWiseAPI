package com.supplywise.supplywise.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.stream.Collectors;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        // Log request details
        log.info("Incoming request: Method={}, URI={}", request.getMethod(), request.getRequestURI());

        // Check authenticated user details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            log.info("Authenticated user: {}", authentication.getName());
            var roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", "));
            log.info("Roles: {}", roles);
        } else {
            log.warn("No authenticated user found for the request.");
        }

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error processing the request: {}", e.getMessage(), e);
        }
    }
}

