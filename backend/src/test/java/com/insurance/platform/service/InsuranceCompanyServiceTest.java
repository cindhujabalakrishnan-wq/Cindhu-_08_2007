package com.insurance.platform.service;

import com.insurance.platform.exception.BadRequestException;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.model.entity.InsuranceCompany;
import com.insurance.platform.repository.InsuranceCompanyRepository;
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
class InsuranceCompanyServiceTest {

    @Mock
    private InsuranceCompanyRepository companyRepository;

    private InsuranceCompanyService companyService;

    @BeforeEach
    void setUp() {
        companyService = new InsuranceCompanyService(companyRepository);
    }

    private InsuranceCompany company(Long id, String code, boolean active) {
        InsuranceCompany company = new InsuranceCompany();
        company.setId(id);
        company.setName("Company " + code);
        company.setCode(code);
        company.setActive(active);
        return company;
    }

    @Test
    void list_activeOnly_returnsOnlyActive() {
        when(companyRepository.findByActiveTrue())
                .thenReturn(List.of(company(1L, "LIC", true)));

        List<InsuranceCompany> result = companyService.list(true);

        assertEquals(1, result.size());
        assertEquals("LIC", result.get(0).getCode());
        verify(companyRepository, never()).findAll();
    }

    @Test
    void list_all_returnsEverything() {
        when(companyRepository.findAll())
                .thenReturn(List.of(company(1L, "LIC", true), company(2L, "OLD", false)));

        assertEquals(2, companyService.list(false).size());
        assertEquals(2, companyService.list(null).size());
    }

    @Test
    void get_found_returnsCompany() {
        when(companyRepository.findById(1L))
                .thenReturn(Optional.of(company(1L, "LIC", true)));

        assertEquals("LIC", companyService.get(1L).getCode());
    }

    @Test
    void get_missing_rejected() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> companyService.get(99L));
    }

    @Test
    void create_duplicateCode_rejected() {
        InsuranceCompany request = company(null, "LIC", true);
        when(companyRepository.findByCode("LIC"))
                .thenReturn(Optional.of(company(1L, "LIC", true)));

        assertThrows(BadRequestException.class, () -> companyService.create(request));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void create_uniqueCode_saves() {
        InsuranceCompany request = company(null, "NEW", true);
        when(companyRepository.findByCode("NEW")).thenReturn(Optional.empty());
        when(companyRepository.save(any(InsuranceCompany.class)))
                .thenAnswer(inv -> {
                    InsuranceCompany c = inv.getArgument(0);
                    c.setId(3L);
                    return c;
                });

        assertEquals(3L, companyService.create(request).getId());
    }

    @Test
    void update_patchesMutableFields() {
        InsuranceCompany existing = company(1L, "LIC", true);
        existing.setContactEmail("old@lic.example");
        when(companyRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(companyRepository.save(any(InsuranceCompany.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        InsuranceCompany patch = new InsuranceCompany();
        patch.setName("LIC Updated");
        patch.setContactEmail("new@lic.example");
        patch.setActive(false);

        InsuranceCompany updated = companyService.update(1L, patch);

        assertEquals("LIC Updated", updated.getName());
        assertEquals("new@lic.example", updated.getContactEmail());
        assertFalse(updated.isActive());
        // untouched fields are preserved
        assertEquals("LIC", updated.getCode());
    }
}
