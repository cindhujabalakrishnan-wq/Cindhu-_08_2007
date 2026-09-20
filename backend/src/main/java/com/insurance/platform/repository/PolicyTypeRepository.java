package com.insurance.platform.repository;

import com.insurance.platform.model.entity.PolicyType;
import com.insurance.platform.model.enums.PolicyCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Persistence access for {@link PolicyType} catalogue entries.
 */
@Repository
public interface PolicyTypeRepository extends JpaRepository<PolicyType, Long> {

    /**
     * Finds a product by its unique code.
     *
     * @param code product code
     * @return matching type, if any
     */
    Optional<PolicyType> findByCode(String code);

    /**
     * Lists all active products.
     *
     * @return active products
     */
    List<PolicyType> findByActiveTrue();

    /**
     * Lists products of a given business category.
     *
     * @param category business category
     * @return matching products
     */
    List<PolicyType> findByCategory(PolicyCategory category);
}
