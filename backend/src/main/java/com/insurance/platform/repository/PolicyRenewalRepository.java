package com.insurance.platform.repository;

import com.insurance.platform.model.entity.PolicyRenewal;
import com.insurance.platform.model.enums.RenewalStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Persistence access for {@link PolicyRenewal} entities.
 */
@Repository
public interface PolicyRenewalRepository extends JpaRepository<PolicyRenewal, Long> {

    /**
     * Lists renewals for a policy, newest first.
     *
     * @param policyId policy id
     * @return renewals
     */
    @Query("select r from PolicyRenewal r where r.policy.id = :policyId order by r.createdAt desc")
    List<PolicyRenewal> findByPolicyId(@Param("policyId") Long policyId);

    /**
     * Lists renewals in the given status.
     *
     * @param status renewal status
     * @return matching renewals
     */
    List<PolicyRenewal> findByStatus(RenewalStatus status);

    /**
     * Paged renewals in the given status.
     *
     * @param status renewal status
     * @param pageable pagination
     * @return page of renewals
     */
    Page<PolicyRenewal> findByStatus(RenewalStatus status, Pageable pageable);

    /**
     * Paged listing of a customer's renewals across all their policies.
     *
     * @param customerId owning user id
     * @param pageable pagination
     * @return page of renewals
     */
    @Query("select r from PolicyRenewal r where r.policy.customer.id = :customerId order by r.createdAt desc")
    Page<PolicyRenewal> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    /**
     * Lists a customer's renewals across all their policies.
     *
     * @param customerId owning user id
     * @return renewals
     */
    @Query("select r from PolicyRenewal r where r.policy.customer.id = :customerId order by r.createdAt desc")
    List<PolicyRenewal> findByPolicyCustomerId(@Param("customerId") Long customerId);

    /**
     * Finds the first renewal for a policy in the given status.
     *
     * @param policyId policy id
     * @param status renewal status
     * @return matching renewal, if any
     */
    @Query("select r from PolicyRenewal r where r.policy.id = :policyId and r.status = :status order by r.createdAt desc")
    java.util.Optional<PolicyRenewal> findFirstByPolicyIdAndStatus(@Param("policyId") Long policyId,
                                                                  @Param("status") RenewalStatus status);

    /**
     * Checks for a renewal in the given status.
     *
     * @param policyId policy id
     * @param status renewal status
     * @return true when such a renewal exists
     */
    @Query("select count(r) > 0 from PolicyRenewal r where r.policy.id = :policyId and r.status = :status")
    boolean existsByPolicyIdAndStatus(@Param("policyId") Long policyId,
                                      @Param("status") RenewalStatus status);

    /**
     * Counts pending renewals awaiting review.
     *
     * @param status renewal status
     * @return count
     */
    long countByStatus(RenewalStatus status);
}
