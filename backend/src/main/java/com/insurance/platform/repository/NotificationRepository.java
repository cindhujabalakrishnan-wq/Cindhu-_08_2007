package com.insurance.platform.repository;

import com.insurance.platform.model.entity.Notification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Persistence access for {@link Notification} entities.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Lists a user's notifications, newest first.
     *
     * @param userId recipient id
     * @return notifications
     */
    @Query("select n from Notification n where n.user.id = :userId order by n.createdAt desc")
    List<Notification> findByUserId(@Param("userId") Long userId);

    /**
     * Paged listing of a user's notifications, newest first.
     *
     * @param userId recipient id
     * @param pageable pagination
     * @return page of notifications
     */
    @Query("select n from Notification n where n.user.id = :userId order by n.createdAt desc")
    Page<Notification> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Lists a user's unread notifications, newest first.
     *
     * @param userId recipient id
     * @return unread notifications
     */
    @Query("select n from Notification n where n.user.id = :userId and n.read = false order by n.createdAt desc")
    List<Notification> findUnreadByUserId(@Param("userId") Long userId);

    /**
     * Counts a user's unread notifications.
     *
     * @param userId recipient id
     * @return unread count
     */
    @Query("select count(n) from Notification n where n.user.id = :userId and n.read = false")
    long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * Counts all unread notifications.
     *
     * @return unread count
     */
    long countByReadFalse();

    /**
     * Checks for an identical unread notification (dedupe).
     *
     * @param userId recipient id
     * @param relatedPolicyId linked policy id (nullable)
     * @param title notification title
     * @param type notification type
     * @return true when a duplicate unread notification exists
     */
    @Query("select count(n) > 0 from Notification n where n.user.id = :userId "
        + "and ((:relatedPolicyId is null and n.relatedPolicyId is null) "
        + "or n.relatedPolicyId = :relatedPolicyId) "
        + "and n.title = :title and n.type = :type and n.read = false")
    boolean existsByUserIdAndPolicyIdAndTitleAndNotificationTypeAndReadFalse(
        @Param("userId") Long userId,
        @Param("relatedPolicyId") Long relatedPolicyId,
        @Param("title") String title,
        @Param("type") com.insurance.platform.model.enums.NotificationType type);
}
