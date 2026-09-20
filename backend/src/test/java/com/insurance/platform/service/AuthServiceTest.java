package com.insurance.platform.service;

import com.insurance.platform.dto.auth.AuthResponse;
import com.insurance.platform.dto.auth.LoginRequest;
import com.insurance.platform.dto.auth.RegisterRequest;
import com.insurance.platform.exception.BadRequestException;
import com.insurance.platform.mapper.UserMapper;
import com.insurance.platform.model.entity.User;
import com.insurance.platform.model.enums.Role;
import com.insurance.platform.repository.CustomerProfileRepository;
import com.insurance.platform.repository.UserRepository;
import com.insurance.platform.security.CustomUserDetailsService;
import com.insurance.platform.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CustomerProfileRepository profileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private AuditLogService auditLogService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, profileRepository, passwordEncoder,
                jwtService, authenticationManager, new UserMapper(), auditLogService);
    }

    private RegisterRequest registerRequest(String email) {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(email);
        req.setPassword("password123");
        req.setFirstName("Jane");
        req.setLastName("Doe");
        req.setPhone("9999999999");
        return req;
    }

    @Test
    void register_success_createsCustomerAndReturnsToken() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(7L);
            return u;
        });
        when(jwtService.generateToken(eq(7L), eq("jane@example.com"), eq("ROLE_CUSTOMER")))
                .thenReturn("jwt-token");

        AuthResponse response = authService.register(registerRequest("jane@example.com"));

        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getUser());
        assertEquals("jane@example.com", response.getUser().getEmail());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("hashed", captor.getValue().getPassword());
        assertEquals(Role.ROLE_CUSTOMER, captor.getValue().getRole());
        assertTrue(captor.getValue().isEnabled());
        verify(profileRepository).save(any());
        verify(auditLogService).record(any(), eq("REGISTER"), eq("User"), eq("7"), any(), any());
    }

    @Test
    void register_duplicateEmail_rejected() {
        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> authService.register(registerRequest("dup@example.com")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_emailIsNormalized() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(), any(), any())).thenReturn("tok");

        authService.register(registerRequest("  Jane@Example.COM "));

        verify(userRepository).existsByEmail("jane@example.com");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("jane@example.com", captor.getValue().getEmail());
    }

    @Test
    void login_success_returnsToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("jane@example.com");
        user.setPassword("hashed");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setRole(Role.ROLE_CUSTOMER);
        user.setEnabled(true);

        CustomUserDetailsService.CustomUserPrincipal principal =
                new CustomUserDetailsService.CustomUserPrincipal(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(1L, "jane@example.com", "ROLE_CUSTOMER"))
                .thenReturn("jwt-token");

        LoginRequest request = new LoginRequest();
        request.setEmail("jane@example.com");
        request.setPassword("password123");

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("jane@example.com", response.getUser().getEmail());
    }

    @Test
    void login_invalidCredentials_rejected() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest request = new LoginRequest();
        request.setEmail("jane@example.com");
        request.setPassword("wrong-password");

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void login_unknownUserAfterAuth_rejected() {
        User user = new User();
        user.setId(1L);
        user.setEmail("ghost@example.com");
        user.setPassword("hashed");
        user.setRole(Role.ROLE_CUSTOMER);
        user.setEnabled(true);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal())
                .thenReturn(new CustomUserDetailsService.CustomUserPrincipal(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setEmail("ghost@example.com");
        request.setPassword("password123");

        assertThrows(BadRequestException.class, () -> authService.login(request));
    }

    @Test
    void me_returnsCurrentUserInfo() {
        User user = new User();
        user.setId(3L);
        user.setEmail("jane@example.com");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setRole(Role.ROLE_CUSTOMER);
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));

        assertEquals("jane@example.com", authService.me("jane@example.com").getEmail());
    }

    @Test
    void me_unknownUser_rejected() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> authService.me("missing@example.com"));
    }
}
