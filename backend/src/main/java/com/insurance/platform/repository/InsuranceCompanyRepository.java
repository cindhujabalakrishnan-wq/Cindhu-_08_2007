package com.insurance.platform.repository;

import com.insurance.platform.model.entity.InsuranceCompany;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Persistence access for {@link InsuranceCompany} entities.
 */
@Repository
public interface InsuranceCompanyRepository extends JpaRepository<InsuranceCompany, Long> {

    /**
     * Finds a carrier by its unique code.
     *
     * @param code carrier code
     * @return matching company, if any
     */
    Optional<InsuranceCompany> findByCode(String code);

    /**
     * Lists all active carriers available for new policies.
     *
     * @return active companies
     */
    List<InsuranceCompany> findByActiveTrue();

    /**
     * Lists all carriers ordered by name.
     *
     * @return companies ordered by name
     */
    List<InsuranceCompany> findAllByOrderByNameAsc();
}
