package com.insurance.platform.service;

import com.insurance.platform.exception.BadRequestException;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.model.entity.InsuranceCompany;
import com.insurance.platform.repository.InsuranceCompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD for insurance companies.
 */
@Service
public class InsuranceCompanyService {

    private final InsuranceCompanyRepository companyRepository;

    public InsuranceCompanyService(InsuranceCompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /** Lists companies, optionally only active ones. */
    @Transactional(readOnly = true)
    public List<InsuranceCompany> list(Boolean activeOnly) {
        if (Boolean.TRUE.equals(activeOnly)) {
            return companyRepository.findByActiveTrue();
        }
        return companyRepository.findAll();
    }

    /** Fetches a single company. */
    @Transactional(readOnly = true)
    public InsuranceCompany get(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance company not found: " + id));
    }

    /** Creates a company with a unique code. */
    @Transactional
    public InsuranceCompany create(InsuranceCompany company) {
        if (company.getCode() != null && companyRepository.findByCode(company.getCode()).isPresent()) {
            throw new BadRequestException("Company code already exists: " + company.getCode());
        }
        return companyRepository.save(company);
    }

    /** Updates a company. */
    @Transactional
    public InsuranceCompany update(Long id, InsuranceCompany patch) {
        InsuranceCompany existing = get(id);
        if (patch.getName() != null) {
            existing.setName(patch.getName());
        }
        if (patch.getCode() != null) {
            existing.setCode(patch.getCode());
        }
        if (patch.getContactEmail() != null) {
            existing.setContactEmail(patch.getContactEmail());
        }
        if (patch.getContactPhone() != null) {
            existing.setContactPhone(patch.getContactPhone());
        }
        if (patch.getWebsite() != null) {
            existing.setWebsite(patch.getWebsite());
        }
        if (patch.getAddress() != null) {
            existing.setAddress(patch.getAddress());
        }
        existing.setActive(patch.isActive());
        return companyRepository.save(existing);
    }
}
