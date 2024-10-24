// package com.supplywise.supplywise.controllers;

// import com.supplywise.supplywise.config.SecurityConfiguration;
// import com.supplywise.supplywise.model.Company;
// import com.supplywise.supplywise.model.Role;
// import com.supplywise.supplywise.model.User;
// import com.supplywise.supplywise.services.CompanyService;
// import com.supplywise.supplywise.services.AuthHandler;
// import com.supplywise.supplywise.services.UserService;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.MockitoAnnotations;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.context.annotation.Import;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;

// import static org.mockito.Mockito.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// import java.util.UUID;

// @WebMvcTest(CompanyController.class)
// @Import(SecurityConfiguration.class) // Ensure that security configuration is loaded - DO NOT REMOVE
// class CompanyControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private CompanyService companyService;

//     @MockBean
//     private AuthHandler authHandler;

//     @MockBean
//     private UserService userService;

//     @InjectMocks
//     private CompanyController companyController;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//     }

//     @Test
//     void testCreateCompany_Success() throws Exception {
//         String companyName = "TechCorp";
//         User user = new User();
//         user.setId(UUID.randomUUID());
//         user.setRole(Role.DISASSOCIATED);

//         // Mock authenticated user and company creation
//         when(authHandler.getAuthenticatedUser()).thenReturn(user);
//         doNothing().when(companyService).createCompany(any(Company.class));
//         doNothing().when(userService).updateUser(any(UUID.class), any(User.class));

//         // Make the request and check if it returns CREATED (201)
//         mockMvc.perform(post("/api/company/create")
//                         .param("name", companyName)
//                         .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isCreated())
//                 .andExpect(content().string("Company created successfully. Role updated to FRANCHISE_OWNER."));
//     }

//     @Test
//     void testCreateCompany_UnauthenticatedUser() throws Exception {
//         String companyName = "TechCorp";

//         // Simulate unauthenticated user
//         when(authHandler.getAuthenticatedUser()).thenReturn(null);

//         // Make the request and check if it returns UNAUTHORIZED (401)
//         mockMvc.perform(post("/api/company/create")
//                         .param("name", companyName)
//                         .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isUnauthorized())
//                 .andExpect(content().string("User is not authenticated."));
//     }

//     @Test
//     void testCreateCompany_IneligibleUser() throws Exception {
//         String companyName = "TechCorp";
//         User user = new User();
//         user.setId(UUID.randomUUID());
//         user.setRole(Role.FRANCHISE_OWNER); // Not allowed to create a company

//         // Mock an authenticated user with an ineligible role
//         when(authHandler.getAuthenticatedUser()).thenReturn(user);

//         // Make the request and check if it returns FORBIDDEN (403)
//         mockMvc.perform(post("/api/company/create")
//                         .param("name", companyName)
//                         .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isForbidden())
//                 .andExpect(content().string("User is not eligible to create a company."));
//     }
// }
