package com.insurance.platform.mapper;

import com.insurance.platform.dto.auth.UserInfo;
import com.insurance.platform.dto.user.UserResponse;
import com.insurance.platform.model.entity.User;
import org.springframework.stereotype.Component;

/**
 * Maps {@link User} entities to user-facing DTOs.
 */
@Component
public class UserMapper {

    /** Maps a user entity to its public response. */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    /** Maps a user entity to the compact auth payload. */
    public UserInfo toUserInfo(User user) {
        if (user == null) {
            return null;
        }
        return new UserInfo(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole() != null ? user.getRole().name() : null);
    }
}
