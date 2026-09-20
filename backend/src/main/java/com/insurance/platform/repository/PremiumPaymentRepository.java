package com.insurance.platform.repository;

import com.insurance.platform.model.entity.PremiumPayment;
import com.insurance.platform.model.enums.PaymentStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Persistence access for {@link PremiumPayment} entities.
 */
@Repository
public interface PremiumPaymentRepository extends JpaRepository<PremiumPayment, Long> {

    /**
     * Lists payments for a policy, newest first.
     *
     * @param policyId policy id
     * @return payments
     */
    @Query("select p from PremiumPayment p where p.policy.id = :policyId order by p.createdAt desc")
    List<PremiumPayment> findByPolicyId(@Param("policyId") Long policyId);

    /**
     * Paged listing of a policy's payments.
     *
     * @param policyId policy id
     * @param pageable pagination
     * @return page of payments
     */
    @Query("select p from PremiumPayment p where p.policy.id = :policyId")
    Page<PremiumPayment> findByPolicyId(@Param("policyId") Long policyId, Pageable pageable);

    /**
     * Lists payments in the given status.
     *
     * @param status payment status
     * @return matching payments
     */
    List<PremiumPayment> findByStatus(PaymentStatus status);

    /**
     * Finds payments due on or before the given date (for reminders).
     *
     * @param date inclusive upper bound
     * @return due payments
     */
    List<PremiumPayment> findByDueDateLessThanEqual(LocalDate date);

    /**
     * Finds pending payments due on or before the given date.
     *
     * @param status payment status
     * @param date inclusive upper bound
     * @return matching payments
     */
    List<PremiumPayment> findByStatusAndDueDateLessThanEqual(PaymentStatus status, LocalDate date);

    /**
     * Paged listing of a customer's payments across all their policies.
     *
     * @param customerId owning user id
     * @param pageable pagination
     * @return page of payments
     */
    @Query("select p from PremiumPayment p where p.policy.customer.id = :customerId")
    Page<PremiumPayment> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    /**
     * Lists a policy's payments in the given status.
     *
     * @param policyId policy id
     * @param status payment status
     * @return matching payments
     */
    @Query("select p from PremiumPayment p where p.policy.id = :policyId and p.status = :status")
    List<PremiumPayment> findByPolicyIdAndStatus(@Param("policyId") Long policyId,
                                                @Param("status") PaymentStatus status);

    /**
     * Checks for a policy payment in the given status.
     *
     * @param policyId policy id
     * @param status payment status
     * @return true when such a payment exists
     */
    @Query("select count(p) > 0 from PremiumPayment p where p.policy.id = :policyId and p.status = :status")
    boolean existsByPolicyIdAndStatus(@Param("policyId") Long policyId,
                                      @Param("status") PaymentStatus status);

    /**
     * Counts payments in the given status.
     *
     * @param status payment status
     * @return payment count
     */
    long countByStatus(PaymentStatus status);
}
