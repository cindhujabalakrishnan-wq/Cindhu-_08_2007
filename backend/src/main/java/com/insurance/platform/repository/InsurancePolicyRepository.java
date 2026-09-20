package com.insurance.platform.repository;

import com.insurance.platform.model.entity.InsurancePolicy;
import com.insurance.platform.model.enums.PolicyStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Persistence access for {@link InsurancePolicy} entities, including expiry
 * monitoring and free-text search used by dashboards.
 */
@Repository
public interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, Long> {

    /**
     * Finds a policy by its unique business number.
     *
     * @param policyNumber unique policy number
     * @return matching policy, if any
     */
    Optional<InsurancePolicy> findByPolicyNumber(String policyNumber);

    /**
     * Checks whether a policy number is already taken.
     *
     * @param policyNumber candidate number
     * @return true when taken
     */
    boolean existsByPolicyNumber(String policyNumber);

    /**
     * Lists all policies owned by a customer.
     *
     * @param customerId owning user id
     * @return customer policies
     */
    @Query("select p from InsurancePolicy p where p.customer.id = :customerId")
    List<InsurancePolicy> findByCustomerId(@Param("customerId") Long customerId);

    /**
     * Paged listing of a customer's policies.
     *
     * @param customerId owning user id
     * @param pageable pagination
     * @return page of policies
     */
    @Query("select p from InsurancePolicy p where p.customer.id = :customerId")
    Page<InsurancePolicy> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    /**
     * Lists all policies currently in the given status.
     *
     * @param status policy status
     * @return matching policies
     */
    List<InsurancePolicy> findByStatus(PolicyStatus status);

    /**
     * Lists a customer's policies in the given status.
     *
     * @param customerId owning user id
     * @param status policy status
     * @return matching policies
     */
    @Query("select p from InsurancePolicy p where p.customer.id = :customerId and p.status = :status")
    List<InsurancePolicy> findByCustomerIdAndStatus(@Param("customerId") Long customerId,
                                                   @Param("status") PolicyStatus status);

    /**
     * Finds policies expiring on or before the given date (for schedulers).
     *
     * @param date inclusive upper bound
     * @return policies expiring soon or already expired
     */
    List<InsurancePolicy> findByExpiryDateLessThanEqual(LocalDate date);

    /**
     * Finds active policies expiring within the given window.
     *
     * @param from lower bound (inclusive)
     * @param to upper bound (inclusive)
     * @return policies expiring in the window
     */
    List<InsurancePolicy> findByStatusAndExpiryDateBetween(PolicyStatus status, LocalDate from, LocalDate to);

    /**
     * Free-text search across policy number, policy name and company name.
     *
     * @param customerId owning user id
     * @param query search term (matched with LIKE)
     * @param pageable pagination
     * @return matching page
     */
    @Query("select p from InsurancePolicy p "
        + "where p.customer.id = :customerId "
        + "and (lower(p.policyNumber) like lower(concat('%', :query, '%')) "
        + "or lower(p.policyName) like lower(concat('%', :query, '%')) "
        + "or lower(p.insuranceCompany.name) like lower(concat('%', :query, '%')))")
    Page<InsurancePolicy> searchByCustomer(@Param("customerId") Long customerId,
                                          @Param("query") String query,
                                          Pageable pageable);

    /**
     * Admin-level free-text search across policy number, policy name and company name.
     *
     * @param query search term (matched with LIKE)
     * @param pageable pagination
     * @return matching page
     */
    @Query("select p from InsurancePolicy p "
        + "where lower(p.policyNumber) like lower(concat('%', :query, '%')) "
        + "or lower(p.policyName) like lower(concat('%', :query, '%')) "
        + "or lower(p.insuranceCompany.name) like lower(concat('%', :query, '%'))")
    Page<InsurancePolicy> searchAll(@Param("query") String query, Pageable pageable);

    /**
     * Finds policies expiring within the given window (for schedulers).
     *
     * @param from lower bound (inclusive)
     * @param to upper bound (inclusive)
     * @return policies expiring in the window
     */
    List<InsurancePolicy> findByExpiryDateBetween(LocalDate from, LocalDate to);

    /**
     * Finds a customer's policies expiring within the given window.
     *
     * @param customerId owning user id
     * @param from lower bound (inclusive)
     * @param to upper bound (inclusive)
     * @return matching policies
     */
    @Query("select p from InsurancePolicy p where p.customer.id = :customerId "
        + "and p.expiryDate between :from and :to")
    List<InsurancePolicy> findByCustomerIdAndExpiryDateBetween(@Param("customerId") Long customerId,
                                                              @Param("from") LocalDate from,
                                                              @Param("to") LocalDate to);

    /**
     * Admin-level filtered search with optional status, category, expiry and free-text.
     *
     * @param status policy status filter (nullable)
     * @param category policy category filter (nullable)
     * @param expiryBefore expiry upper bound filter (nullable)
     * @param query free-text query (nullable)
     * @param pageable pagination
     * @return matching page
     */
    @Query("select p from InsurancePolicy p "
        + "where (:status is null or p.status = :status) "
        + "and (:category is null or p.category = :category) "
        + "and (:expiryBefore is null or p.expiryDate <= :expiryBefore) "
        + "and (:query is null or lower(p.policyNumber) like lower(concat('%', :query, '%')) "
        + "or lower(p.policyName) like lower(concat('%', :query, '%')) "
        + "or lower(p.insuranceCompany.name) like lower(concat('%', :query, '%')))")
    Page<InsurancePolicy> searchAllFiltered(@Param("status") PolicyStatus status,
                                           @Param("category") com.insurance.platform.model.enums.PolicyCategory category,
                                           @Param("expiryBefore") LocalDate expiryBefore,
                                           @Param("query") String query,
                                           Pageable pageable);

    /**
     * Customer-level filtered search with optional status, category, expiry and free-text.
     *
     * @param customerId owning user id
     * @param status policy status filter (nullable)
     * @param category policy category filter (nullable)
     * @param expiryBefore expiry upper bound filter (nullable)
     * @param query free-text query (nullable)
     * @param pageable pagination
     * @return matching page
     */
    @Query("select p from InsurancePolicy p "
        + "where p.customer.id = :customerId "
        + "and (:status is null or p.status = :status) "
        + "and (:category is null or p.category = :category) "
        + "and (:expiryBefore is null or p.expiryDate <= :expiryBefore) "
        + "and (:query is null or lower(p.policyNumber) like lower(concat('%', :query, '%')) "
        + "or lower(p.policyName) like lower(concat('%', :query, '%')) "
        + "or lower(p.insuranceCompany.name) like lower(concat('%', :query, '%')))")
    Page<InsurancePolicy> searchByCustomerFiltered(@Param("customerId") Long customerId,
                                                  @Param("status") PolicyStatus status,
                                                  @Param("category") com.insurance.platform.model.enums.PolicyCategory category,
                                                  @Param("expiryBefore") LocalDate expiryBefore,
                                                  @Param("query") String query,
                                                  Pageable pageable);

    /**
     * Counts policies owned by a customer.
     *
     * @param customerId owning user id
     * @return policy count
     */
    @Query("select count(p) from InsurancePolicy p where p.customer.id = :customerId")
    long countByCustomerId(@Param("customerId") Long customerId);

    /**
     * Counts a customer's policies in the given status.
     *
     * @param customerId owning user id
     * @param status policy status
     * @return policy count
     */
    @Query("select count(p) from InsurancePolicy p where p.customer.id = :customerId and p.status = :status")
    long countByCustomerIdAndStatus(@Param("customerId") Long customerId,
                                   @Param("status") PolicyStatus status);

    /**
     * Counts all policies in the given status.
     *
     * @param status policy status
     * @return policy count
     */
    long countByStatus(PolicyStatus status);
}
