package com.insurance.platform.service;

import com.insurance.platform.exception.BadRequestException;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.model.entity.PolicyType;
import com.insurance.platform.model.enums.PolicyCategory;
import com.insurance.platform.repository.PolicyTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyTypeServiceTest {

    @Mock
    private PolicyTypeRepository policyTypeRepository;

    private PolicyTypeService policyTypeService;

    @BeforeEach
    void setUp() {
        policyTypeService = new PolicyTypeService(policyTypeRepository);
    }

    private PolicyType type(Long id, String code, PolicyCategory category, boolean active) {
        PolicyType type = new PolicyType();
        type.setId(id);
        type.setName("Type " + code);
        type.setCode(code);
        type.setCategory(category);
        type.setActive(active);
        return type;
    }

    @Test
    void list_byCategory_filters() {
        when(policyTypeRepository.findByCategory(PolicyCategory.HEALTH))
                .thenReturn(List.of(type(1L, "HLTH", PolicyCategory.HEALTH, true)));

        List<PolicyType> result =
                policyTypeService.list(PolicyCategory.HEALTH, null);

        assertEquals(1, result.size());
        assertEquals(PolicyCategory.HEALTH, result.get(0).getCategory());
    }

    @Test
    void list_activeOnly_returnsActive() {
        when(policyTypeRepository.findByActiveTrue())
                .thenReturn(List.of(type(1L, "HLTH", PolicyCategory.HEALTH, true)));

        assertEquals(1, policyTypeService.list(null, true).size());
        verify(policyTypeRepository, never()).findAll();
    }

    @Test
    void list_all_returnsEverything() {
        when(policyTypeRepository.findAll())
                .thenReturn(List.of(type(1L, "A", PolicyCategory.LIFE, true),
                        type(2L, "B", PolicyCategory.AUTO, false)));

        assertEquals(2, policyTypeService.list(null, null).size());
    }

    @Test
    void get_missing_rejected() {
        when(policyTypeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> policyTypeService.get(99L));
    }

    @Test
    void create_duplicateCode_rejected() {
        PolicyType request = type(null, "HLTH", PolicyCategory.HEALTH, true);
        when(policyTypeRepository.findByCode("HLTH"))
                .thenReturn(Optional.of(type(1L, "HLTH", PolicyCategory.HEALTH, true)));

        assertThrows(BadRequestException.class, () -> policyTypeService.create(request));
        verify(policyTypeRepository, never()).save(any());
    }

    @Test
    void create_uniqueCode_saves() {
        PolicyType request = type(null, "NEW", PolicyCategory.TRAVEL, true);
        when(policyTypeRepository.findByCode("NEW")).thenReturn(Optional.empty());
        when(policyTypeRepository.save(any(PolicyType.class)))
                .thenAnswer(inv -> {
                    PolicyType t = inv.getArgument(0);
                    t.setId(5L);
                    return t;
                });

        assertEquals(5L, policyTypeService.create(request).getId());
    }

    @Test
    void update_patchesMutableFields() {
        PolicyType existing = type(1L, "HLTH", PolicyCategory.HEALTH, true);
        when(policyTypeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(policyTypeRepository.save(any(PolicyType.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PolicyType patch = new PolicyType();
        patch.setName("Health Renovated");
        patch.setCategory(PolicyCategory.LIFE);
        patch.setDescription("Updated description");
        patch.setActive(false);

        PolicyType updated = policyTypeService.update(1L, patch);

        assertEquals("Health Renovated", updated.getName());
        assertEquals(PolicyCategory.LIFE, updated.getCategory());
        assertEquals("Updated description", updated.getDescription());
        assertFalse(updated.isActive());
        assertEquals("HLTH", updated.getCode());
    }
}
