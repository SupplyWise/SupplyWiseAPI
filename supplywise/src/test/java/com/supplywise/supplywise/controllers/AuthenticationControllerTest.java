package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.services.AuthHandler;
import com.supplywise.supplywise.config.JwtAuthenticationFilter;
import com.supplywise.supplywise.config.SecurityConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class})
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthHandler authHandler;

    @InjectMocks
    private AuthenticationController authenticationController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetUserRoles() throws Exception {
        List<String> expectedRoles = List.of("ROLE_ADMIN", "ROLE_DISASSOCIATED");
        when(authHandler.getAuthenticatedUserRoles()).thenReturn(expectedRoles);

        mockMvc.perform(get("/api/auth/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedRoles.toString()));
    }
}
