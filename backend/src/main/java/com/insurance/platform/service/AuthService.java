package com.insurance.platform.service;

import com.insurance.platform.dto.auth.AuthResponse;
import com.insurance.platform.dto.auth.LoginRequest;
import com.insurance.platform.dto.auth.RegisterRequest;
import com.insurance.platform.dto.auth.UserInfo;
import com.insurance.platform.exception.BadRequestException;
import com.insurance.platform.mapper.UserMapper;
import com.insurance.platform.model.entity.CustomerProfile;
import com.insurance.platform.model.entity.User;
import com.insurance.platform.model.enums.Role;
import com.insurance.platform.repository.CustomerProfileRepository;
import com.insurance.platform.repository.UserRepository;
import com.insurance.platform.security.CustomUserDetailsService;
import com.insurance.platform.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Authentication use-cases: registration, login and current-user lookup.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final CustomerProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    public AuthService(UserRepository userRepository,
                       CustomerProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       UserMapper userMapper,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
        this.auditLogService = auditLogService;
    }

    /**
     * Registers a new customer: BCrypt-hashes the password, creates an empty
     * profile and returns a signed JWT.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(Role.ROLE_CUSTOMER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        CustomerProfile profile = new CustomerProfile();
        profile.setUser(user);
        profileRepository.save(profile);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        UserInfo info = userMapper.toUserInfo(user);
        auditLogService.record(user, "REGISTER", "User", String.valueOf(user.getId()),
                "User registered", null);
        log.info("User registered: {}", email);
        return new AuthResponse(token, info);
    }

    /** Authenticates with email/password and returns a signed JWT. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        CustomUserDetailsService.CustomUserPrincipal principal =
                (CustomUserDetailsService.CustomUserPrincipal) auth.getPrincipal();
        String token = jwtService.generateToken(principal.getUserId(), principal.getUsername(), principal.getRole());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        log.info("User logged in: {}", email);
        return new AuthResponse(token, userMapper.toUserInfo(user));
    }

    /** Returns the current authenticated user's info. */
    @Transactional(readOnly = true)
    public UserInfo me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return userMapper.toUserInfo(user);
    }
}
