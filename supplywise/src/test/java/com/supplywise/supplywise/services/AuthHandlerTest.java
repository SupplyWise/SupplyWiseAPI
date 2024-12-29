package com.supplywise.supplywise.services;

import com.supplywise.supplywise.config.CustomAuthenticationDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthHandlerTest {

    private AuthHandler authHandler;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authHandler = new AuthHandler();
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetAuthenticatedCognitoSub() {
        String expectedSub = "cognito-sub-example";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(expectedSub);

        String actualSub = authHandler.getAuthenticatedCognitoSub();

        assertEquals(expectedSub, actualSub);
        verify(securityContext).getAuthentication();
        verify(authentication).getPrincipal();
    }

    @Test
    void testGetAuthenticatedAccessToken() {
        String expectedToken = "access-token-example";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getCredentials()).thenReturn(expectedToken);

        String actualToken = authHandler.getAuthenticatedAccessToken();

        assertEquals(expectedToken, actualToken);
        verify(securityContext).getAuthentication();
        verify(authentication).getCredentials();
    }

    @Test
    void testGetAuthenticatedUserRoles() {
        // Create and configure the security context
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        
        // Create test authentication with explicit authority
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_MANAGER"));
        TestingAuthenticationToken testAuth = new TestingAuthenticationToken(
            "user", 
            "password", 
            authorities
        );
        testAuth.setAuthenticated(true);
        
        // Set the authentication in the context
        context.setAuthentication(testAuth);
        SecurityContextHolder.setContext(context);
        
        try {
            // Call the method under test
            List<String> roles = authHandler.getAuthenticatedUserRoles();
            
            // Assert the roles are correct
            assertEquals(List.of("ROLE_MANAGER"), roles);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void testGetAuthenticatedCompanyId() {
        CustomAuthenticationDetails details = mock(CustomAuthenticationDetails.class);
        when(details.getCompanyId()).thenReturn("company-id-example");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn(details);

        String companyId = authHandler.getAuthenticatedCompanyId();

        assertEquals("company-id-example", companyId);
        verify(securityContext).getAuthentication();
        verify(authentication).getDetails();
        verify(details).getCompanyId();
    }

    @Test
    void testGetAuthenticatedRestaurantId() {
        CustomAuthenticationDetails details = mock(CustomAuthenticationDetails.class);
        when(details.getRestaurantId()).thenReturn("restaurant-id-example");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn(details);

        String restaurantId = authHandler.getAuthenticatedRestaurantId();

        assertEquals("restaurant-id-example", restaurantId);
        verify(securityContext).getAuthentication();
        verify(authentication).getDetails();
        verify(details).getRestaurantId();
    }

    @Test
    void testGetAuthenticatedUsername() {
        CustomAuthenticationDetails details = mock(CustomAuthenticationDetails.class);
        when(details.getUsername()).thenReturn("username-example");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn(details);

        String username = authHandler.getAuthenticatedUsername();

        assertEquals("username-example", username);
        verify(securityContext).getAuthentication();
        verify(authentication).getDetails();
        verify(details).getUsername();
    }

}