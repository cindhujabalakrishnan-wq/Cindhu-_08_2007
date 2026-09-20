package com.insurance.platform.service;

import com.insurance.platform.exception.BadRequestException;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.model.entity.PolicyType;
import com.insurance.platform.model.enums.PolicyCategory;
import com.insurance.platform.repository.PolicyTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD for policy types.
 */
@Service
public class PolicyTypeService {

    private final PolicyTypeRepository policyTypeRepository;

    public PolicyTypeService(PolicyTypeRepository policyTypeRepository) {
        this.policyTypeRepository = policyTypeRepository;
    }

    /** Lists policy types, optionally filtered by category or active flag. */
    @Transactional(readOnly = true)
    public List<PolicyType> list(PolicyCategory category, Boolean activeOnly) {
        if (category != null) {
            return policyTypeRepository.findByCategory(category);
        }
        if (Boolean.TRUE.equals(activeOnly)) {
            return policyTypeRepository.findByActiveTrue();
        }
        return policyTypeRepository.findAll();
    }

    /** Fetches a single policy type. */
    @Transactional(readOnly = true)
    public PolicyType get(Long id) {
        return policyTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy type not found: " + id));
    }

    /** Creates a policy type with a unique code. */
    @Transactional
    public PolicyType create(PolicyType type) {
        if (type.getCode() != null && policyTypeRepository.findByCode(type.getCode()).isPresent()) {
            throw new BadRequestException("Policy type code already exists: " + type.getCode());
        }
        return policyTypeRepository.save(type);
    }

    /** Updates a policy type. */
    @Transactional
    public PolicyType update(Long id, PolicyType patch) {
        PolicyType existing = get(id);
        if (patch.getName() != null) {
            existing.setName(patch.getName());
        }
        if (patch.getCode() != null) {
            existing.setCode(patch.getCode());
        }
        if (patch.getCategory() != null) {
            existing.setCategory(patch.getCategory());
        }
        if (patch.getDescription() != null) {
            existing.setDescription(patch.getDescription());
        }
        existing.setActive(patch.isActive());
        return policyTypeRepository.save(existing);
    }
}
