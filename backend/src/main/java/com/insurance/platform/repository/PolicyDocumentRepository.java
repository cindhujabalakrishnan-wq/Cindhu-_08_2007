package com.insurance.platform.repository;

import com.insurance.platform.model.entity.PolicyDocument;
import com.insurance.platform.model.enums.DocumentType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Persistence access for {@link PolicyDocument} entities.
 */
@Repository
public interface PolicyDocumentRepository extends JpaRepository<PolicyDocument, Long> {

    /**
     * Lists documents for a policy, newest first.
     *
     * @param policyId policy id
     * @return documents
     */
    @Query("select d from PolicyDocument d where d.policy.id = :policyId order by d.createdAt desc")
    List<PolicyDocument> findByPolicyId(@Param("policyId") Long policyId);

    /**
     * Lists a policy's documents of the given type.
     *
     * @param policyId policy id
     * @param documentType document classification
     * @return matching documents
     */
    @Query("select d from PolicyDocument d where d.policy.id = :policyId and d.documentType = :documentType")
    List<PolicyDocument> findByPolicyIdAndDocumentType(@Param("policyId") Long policyId,
                                                      @Param("documentType") DocumentType documentType);

    /**
     * Counts documents for a policy.
     *
     * @param policyId policy id
     * @return document count
     */
    @Query("select count(d) from PolicyDocument d where d.policy.id = :policyId")
    long countByPolicyId(@Param("policyId") Long policyId);
}
