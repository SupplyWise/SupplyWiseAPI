package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Company;
import com.supplywise.supplywise.repositories.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    public Company createCompany(Company company) {
        return companyRepository.save(company);
    }

    public boolean companyExists(UUID id) {
        return companyRepository.existsById(id);
    }
}
