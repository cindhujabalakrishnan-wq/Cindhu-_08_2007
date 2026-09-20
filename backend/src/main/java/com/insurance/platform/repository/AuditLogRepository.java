package com.insurance.platform.repository;

import com.insurance.platform.model.entity.AuditLog;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Persistence access for {@link AuditLog} entries.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Lists audit entries for a user, newest first.
     *
     * @param userId acting user id
     * @param pageable pagination
     * @return page of entries
     */
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Lists audit entries for an entity instance, newest first.
     *
     * @param entityType entity type
     * @param entityId entity id
     * @return matching entries
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);
}
