package com.insurance.platform.service;

import com.insurance.platform.dto.notification.NotificationResponse;
import com.insurance.platform.exception.ForbiddenException;
import com.insurance.platform.model.entity.InsurancePolicy;
import com.insurance.platform.model.entity.Notification;
import com.insurance.platform.model.entity.User;
import com.insurance.platform.model.enums.NotificationType;
import com.insurance.platform.model.enums.Role;
import com.insurance.platform.repository.NotificationRepository;
import com.insurance.platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, userRepository);
    }

    private User user(long id, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setRole(role);
        user.setEnabled(true);
        return user;
    }

    private Notification notification(long id, User owner, String title, boolean read) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUser(owner);
        notification.setRelatedPolicyId(5L);
        notification.setTitle(title);
        notification.setMessage("body");
        notification.setType(NotificationType.POLICY_EXPIRY);
        notification.setRead(read);
        return notification;
    }

    @Test
    void notify_createsAndSaves() {
        User owner = user(1L, "jane@example.com", Role.ROLE_CUSTOMER);
        InsurancePolicy policy = new InsurancePolicy();
        policy.setId(5L);
        when(notificationRepository.existsByUserIdAndPolicyIdAndTitleAndNotificationTypeAndReadFalse(
                eq(1L), eq(5L), eq("Expiring"), eq(NotificationType.POLICY_EXPIRY)))
                .thenReturn(false);
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> {
                    Notification n = inv.getArgument(0);
                    n.setId(31L);
                    return n;
                });

        NotificationResponse response = notificationService.notify(owner, policy,
                "Expiring", "Your policy expires soon", NotificationType.POLICY_EXPIRY);

        assertNotNull(response);
        assertEquals(31L, response.getId());
        assertEquals(5L, response.getPolicyId());
        assertEquals("Expiring", response.getTitle());
        assertFalse(response.isRead());
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(owner, captor.getValue().getUser());
    }

    @Test
    void notify_duplicateUnread_returnsNullWithoutSaving() {
        User owner = user(1L, "jane@example.com", Role.ROLE_CUSTOMER);
        InsurancePolicy policy = new InsurancePolicy();
        policy.setId(5L);
        when(notificationRepository.existsByUserIdAndPolicyIdAndTitleAndNotificationTypeAndReadFalse(
                eq(1L), eq(5L), eq("Expiring"), eq(NotificationType.POLICY_EXPIRY)))
                .thenReturn(true);

        assertNull(notificationService.notify(owner, policy, "Expiring",
                "Your policy expires soon", NotificationType.POLICY_EXPIRY));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void notify_nullUser_returnsNull() {
        assertNull(notificationService.notify(null, null, "t", "m", NotificationType.SYSTEM));
        verifyNoInteractions(notificationRepository);
    }

    @Test
    void list_returnsMappedNotifications() {
        User owner = user(1L, "jane@example.com", Role.ROLE_CUSTOMER);
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(owner));
        when(notificationRepository.findByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification(31L, owner, "A", false))));

        List<NotificationResponse> result = notificationService.list("jane@example.com");

        assertEquals(1, result.size());
        assertEquals(31L, result.get(0).getId());
    }

    @Test
    void unread_returnsOnlyUnread() {
        User owner = user(1L, "jane@example.com", Role.ROLE_CUSTOMER);
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(owner));
        when(notificationRepository.findUnreadByUserId(1L))
                .thenReturn(List.of(notification(31L, owner, "A", false)));

        List<NotificationResponse> result = notificationService.unread("jane@example.com");

        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());
    }

    @Test
    void markRead_success_marksRead() {
        User owner = user(1L, "jane@example.com", Role.ROLE_CUSTOMER);
        Notification existing = notification(31L, owner, "A", false);
        when(notificationRepository.findById(31L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(owner));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse response =
                notificationService.markRead("jane@example.com", 31L);

        assertTrue(response.isRead());
        assertTrue(existing.isRead());
    }

    @Test
    void markRead_forbiddenForNonOwner() {
        User owner = user(1L, "owner@example.com", Role.ROLE_CUSTOMER);
        User intruder = user(2L, "intruder@example.com", Role.ROLE_CUSTOMER);
        when(notificationRepository.findById(31L))
                .thenReturn(Optional.of(notification(31L, owner, "A", false)));
        when(userRepository.findByEmail("intruder@example.com"))
                .thenReturn(Optional.of(intruder));

        assertThrows(ForbiddenException.class,
                () -> notificationService.markRead("intruder@example.com", 31L));
    }

    @Test
    void markAllRead_marksEveryUnread() {
        User owner = user(1L, "jane@example.com", Role.ROLE_CUSTOMER);
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(owner));
        Notification first = notification(31L, owner, "A", false);
        Notification second = notification(32L, owner, "B", false);
        when(notificationRepository.findUnreadByUserId(1L))
                .thenReturn(List.of(first, second));

        notificationService.markAllRead("jane@example.com");

        assertTrue(first.isRead());
        assertTrue(second.isRead());
        verify(notificationRepository).saveAll(List.of(first, second));
    }
}
