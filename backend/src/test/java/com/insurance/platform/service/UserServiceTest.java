package com.insurance.platform.service;

import com.insurance.platform.dto.user.UpdateUserRequest;
import com.insurance.platform.dto.user.UserResponse;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.mapper.UserMapper;
import com.insurance.platform.model.entity.User;
import com.insurance.platform.model.enums.Role;
import com.insurance.platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, new UserMapper());
    }

    private User user(long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setPhone("111");
        user.setRole(Role.ROLE_CUSTOMER);
        user.setEnabled(true);
        return user;
    }

    @Test
    void getByEmail_success() {
        when(userRepository.findByEmail("jane@example.com"))
                .thenReturn(Optional.of(user(1L, "jane@example.com")));

        UserResponse response = userService.getByEmail("jane@example.com");

        assertEquals(1L, response.getId());
        assertEquals("jane@example.com", response.getEmail());
    }

    @Test
    void getByEmail_missing_rejected() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> userService.getByEmail("missing@example.com"));
    }

    @Test
    void updateSelf_updatesMutableFields() {
        User existing = user(1L, "jane@example.com");
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Janet");
        request.setPhone("222");

        UserResponse response = userService.updateSelf("jane@example.com", request);

        assertEquals("Janet", response.getFirstName());
        assertEquals("222", response.getPhone());
        assertEquals("Doe", response.getLastName());
    }

    @Test
    void listAll_mapsPage() {
        when(userRepository.findAll(PageRequest.of(0, 10))).thenReturn(
                new PageImpl<>(List.of(user(1L, "a@example.com"), user(2L, "b@example.com"))));

        assertEquals(2, userService.listAll(PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    void setEnabled_disablesAccount() {
        User existing = user(1L, "jane@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertFalse(userService.setEnabled(1L, false).isEnabled());
        assertTrue(userService.setEnabled(1L, true).isEnabled());
    }

    @Test
    void setEnabled_missing_rejected() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.setEnabled(99L, false));
    }
}
