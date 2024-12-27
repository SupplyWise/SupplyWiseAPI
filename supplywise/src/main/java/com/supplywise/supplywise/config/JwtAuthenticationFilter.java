package com.supplywise.supplywise.config;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nimbusds.jwt.JWTClaimsSet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger_ = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null && CognitoTokenValidator.validateToken(jwt)) {
            JWTClaimsSet claims = CognitoTokenValidator.getClaims(jwt);
            String username = claims.getSubject(); // Cognito username

            logger_.info("Authenticated user: {}", username);

            List<String> roles = null;
            String companyId = null;
            String restaurantId = null;
            try {
                roles = claims.getStringListClaim("cognito:groups"); // Cognito groups
                companyId = claims.getStringClaim("company_id");
                restaurantId = claims.getStringClaim("restaurant_id");
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (username != null && roles != null) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    username, jwt, roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .toList()
                );

                // Add custom attributes to the authentication details
                CustomAuthenticationDetails customDetails = new CustomAuthenticationDetails(
                    username, companyId, restaurantId, roles
                );

                authToken.setDetails(customDetails);
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }
        }
        filterChain.doFilter(request, response);
    }
}
