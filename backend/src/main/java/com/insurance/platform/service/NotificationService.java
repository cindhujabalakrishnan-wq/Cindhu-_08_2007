package com.insurance.platform.service;

import com.insurance.platform.dto.notification.NotificationResponse;
import com.insurance.platform.exception.ForbiddenException;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.model.entity.InsurancePolicy;
import com.insurance.platform.model.entity.Notification;
import com.insurance.platform.model.entity.User;
import com.insurance.platform.model.enums.NotificationType;
import com.insurance.platform.model.enums.Role;
import com.insurance.platform.repository.NotificationRepository;
import com.insurance.platform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Notifications with dedupe and optional email delivery.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private JavaMailSender mailSender;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /** Optionally injects the mail sender when mail is configured. */
    @Autowired(required = false)
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Creates a notification unless an identical unread one already exists (dedupe).
     *
     * @return the created (or existing duplicate) notification, or null when user is missing
     */
    @Transactional
    public NotificationResponse notify(User user, InsurancePolicy policy, String title,
                                       String message, NotificationType type) {
        if (user == null) {
            return null;
        }
        Long policyId = policy != null ? policy.getId() : null;
        if (policyId != null && notificationRepository
                .existsByUserIdAndPolicyIdAndTitleAndNotificationTypeAndReadFalse(
                        user.getId(), policyId, title, type)) {
            log.debug("Deduped notification '{}' for user {}", title, user.getEmail());
            return null;
        }
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setRelatedPolicyId(policyId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        Notification saved = notificationRepository.save(notification);
        sendEmail(user.getEmail(), title, message);
        return toResponse(saved);
    }

    /** Lists the caller's notifications (admins may pass another user's email via lookup). */
    @Transactional(readOnly = true)
    public List<NotificationResponse> list(String email) {
        User user = requireUser(email);
        return notificationRepository.findByUserId(user.getId(), org.springframework.data.domain.Pageable.unpaged())
                .stream().map(this::toResponse).toList();
    }

    /** Lists unread notifications. */
    @Transactional(readOnly = true)
    public List<NotificationResponse> unread(String email) {
        User user = requireUser(email);
        return notificationRepository.findUnreadByUserId(user.getId())
                .stream().map(this::toResponse).toList();
    }

    /** Marks one notification as read after an ownership check. */
    @Transactional
    public NotificationResponse markRead(String email, Long id) {
        Notification notification = requireOwned(email, id);
        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    /** Marks all of the caller's notifications as read. */
    @Transactional
    public void markAllRead(String email) {
        User user = requireUser(email);
        List<Notification> unread = notificationRepository.findUnreadByUserId(user.getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    /** Sends an email when a sender is configured, otherwise logs the intent. */
    public void sendEmail(String to, String subject, String body) {
        if (mailSender == null) {
            log.info("Email disabled; would send to {} subject '{}'", to, subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }

    private Notification requireOwned(String email, Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
        User user = requireUser(email);
        if (user.getRole() != Role.ROLE_ADMIN
                && (notification.getUser() == null || !notification.getUser().getId().equals(user.getId()))) {
            throw new ForbiddenException("You do not have access to this notification");
        }
        return notification;
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse dto = new NotificationResponse();
        dto.setId(notification.getId());
        dto.setPolicyId(notification.getRelatedPolicyId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setNotificationType(notification.getType() != null
                ? notification.getType().name() : null);
        dto.setRead(notification.isRead());
        return dto;
    }
}
