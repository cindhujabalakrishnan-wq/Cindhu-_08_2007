package com.insurance.platform.mapper;

import com.insurance.platform.dto.policy.PolicyResponse;
import com.insurance.platform.model.entity.InsurancePolicy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Maps {@link InsurancePolicy} entities to response DTOs.
 */
@Component
public class PolicyMapper {

    /** Maps a policy entity to its response, including derived days-until-expiry. */
    public PolicyResponse toResponse(InsurancePolicy policy) {
        if (policy == null) {
            return null;
        }
        PolicyResponse dto = new PolicyResponse();
        dto.setId(policy.getId());
        dto.setPolicyNumber(policy.getPolicyNumber());
        if (policy.getCustomer() != null) {
            dto.setCustomerId(policy.getCustomer().getId());
            dto.setCustomerEmail(policy.getCustomer().getEmail());
        }
        if (policy.getInsuranceCompany() != null) {
            dto.setInsuranceCompanyId(policy.getInsuranceCompany().getId());
            dto.setInsuranceCompanyName(policy.getInsuranceCompany().getName());
        }
        if (policy.getPolicyType() != null) {
            dto.setPolicyTypeId(policy.getPolicyType().getId());
            dto.setPolicyTypeName(policy.getPolicyType().getName());
            if (policy.getPolicyType().getCategory() != null) {
                dto.setPolicyCategory(policy.getPolicyType().getCategory().name());
            }
        }
        dto.setPolicyName(policy.getPolicyName());
        dto.setStartDate(policy.getStartDate());
        dto.setExpiryDate(policy.getExpiryDate());
        dto.setPremiumAmount(policy.getPremiumAmount());
        dto.setPremiumFrequency(policy.getPremiumFrequency() != null
                ? policy.getPremiumFrequency().name() : null);
        dto.setCoverageAmount(policy.getCoverageAmount());
        dto.setStatus(policy.getStatus() != null ? policy.getStatus().name() : null);
        dto.setNomineeName(policy.getNomineeName());
        dto.setNomineeContact(policy.getNomineeContact());
        dto.setNotes(policy.getNotes());
        if (policy.getExpiryDate() != null) {
            dto.setDaysUntilExpiry(ChronoUnit.DAYS.between(LocalDate.now(), policy.getExpiryDate()));
        }
        return dto;
    }
}
