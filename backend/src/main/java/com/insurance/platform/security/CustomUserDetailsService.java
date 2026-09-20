package com.insurance.platform.security;

import com.insurance.platform.model.entity.User;
import com.insurance.platform.model.enums.Role;
import com.insurance.platform.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * Loads users by email for Spring Security authentication.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return new CustomUserPrincipal(user);
    }

    /**
     * {@link UserDetails} wrapper exposing userId and role for JWT creation.
     */
    public static class CustomUserPrincipal implements UserDetails {

        private final Long userId;
        private final String email;
        private final String passwordHash;
        private final String role;
        private final boolean enabled;

        public CustomUserPrincipal(User user) {
            this.userId = user.getId();
            this.email = user.getEmail();
            this.passwordHash = user.getPassword();
            this.role = user.getRole() != null ? user.getRole().name() : Role.ROLE_CUSTOMER.name();
            this.enabled = user.isEnabled();
        }

        public Long getUserId() {
            return userId;
        }

        public String getRole() {
            return role;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            String authority = role != null && role.startsWith("ROLE_") ? role : "ROLE_" + role;
            return List.of(new SimpleGrantedAuthority(authority));
        }

        @Override
        public String getPassword() {
            return passwordHash;
        }

        @Override
        public String getUsername() {
            return email;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }
}
