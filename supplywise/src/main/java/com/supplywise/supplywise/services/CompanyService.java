package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Company;
import com.supplywise.supplywise.repositories.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company createCompany(Company company) {
        return companyRepository.save(company);
    }

    public boolean companyExists(UUID id) {
        return companyRepository.existsById(id);
    }

    public Company getCompanyById(UUID id) {
        return companyRepository.findById(id).orElse(null);
    }
}
