package com.insurance.platform.service;

import com.insurance.platform.dto.user.UpdateUserRequest;
import com.insurance.platform.dto.user.UserResponse;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.mapper.UserMapper;
import com.insurance.platform.model.entity.User;
import com.insurance.platform.model.enums.Role;
import com.insurance.platform.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User administration and self-service profile updates.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /** Finds a user by email or throws. */
    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        return userMapper.toResponse(requireByEmail(email));
    }

    /** Updates mutable fields of the current user. */
    @Transactional
    public UserResponse updateSelf(String email, UpdateUserRequest request) {
        User user = requireByEmail(email);
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    /** Paged user list for admins. */
    @Transactional(readOnly = true)
    public Page<UserResponse> listAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    /** Enables or disables a user account. */
    @Transactional
    public UserResponse setEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setEnabled(enabled);
        return userMapper.toResponse(userRepository.save(user));
    }

    /** Changes a user's role. */
    @Transactional
    public UserResponse setRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setRole(role);
        return userMapper.toResponse(userRepository.save(user));
    }

    /** Deletes a user account. */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        userRepository.delete(user);
    }

    private User requireByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
