package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.Company;
import com.supplywise.supplywise.model.Role;
import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.services.CompanyService;
import com.supplywise.supplywise.services.AuthHandler;
import com.supplywise.supplywise.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private AuthHandler authHandler;

    @Autowired
    private UserService userService;


    @PostMapping("/create")
    public ResponseEntity<String> createCompany(@RequestParam String name) {
        
        User user = authHandler.getAuthenticatedUser();

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated.");
        }

        if (user.getRole() != Role.DISASSOCIATED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not eligible to create a company.");
        }

        Company company = new Company(name, user.getId());
        companyService.createCompany(company);

        user.setRole(Role.FRANCHISE_OWNER);  
        userService.updateUser(user.getId(), user); 
              

        return ResponseEntity.status(HttpStatus.CREATED).body("Company created successfully. Role updated to FRANCHISE_OWNER.");
    }
}
