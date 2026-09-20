package com.insurance.platform.mapper;

import com.insurance.platform.dto.renewal.RenewalResponse;
import com.insurance.platform.model.entity.PolicyRenewal;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Maps {@link PolicyRenewal} entities to response DTOs.
 */
@Component
public class RenewalMapper {

    /** Maps a renewal entity to its response, including derived days-remaining. */
    public RenewalResponse toResponse(PolicyRenewal renewal) {
        if (renewal == null) {
            return null;
        }
        RenewalResponse dto = new RenewalResponse();
        dto.setId(renewal.getId());
        if (renewal.getPolicy() != null) {
            dto.setPolicyId(renewal.getPolicy().getId());
            dto.setPolicyNumber(renewal.getPolicy().getPolicyNumber());
        }
        dto.setPreviousExpiryDate(renewal.getPreviousExpiryDate());
        dto.setRenewalDate(renewal.getRequestedAt() != null
                ? renewal.getRequestedAt().toLocalDate() : null);
        dto.setNewExpiryDate(renewal.getNewExpiryDate());
        dto.setRenewalPremium(renewal.getRenewalPremium());
        dto.setStatus(renewal.getStatus() != null ? renewal.getStatus().name() : null);
        dto.setNotes(renewal.getRemarks());
        LocalDate anchor = renewal.getNewExpiryDate() != null
                ? renewal.getNewExpiryDate()
                : renewal.getPreviousExpiryDate();
        if (anchor != null) {
            dto.setDaysRemaining(ChronoUnit.DAYS.between(LocalDate.now(), anchor));
        }
        return dto;
    }
}
