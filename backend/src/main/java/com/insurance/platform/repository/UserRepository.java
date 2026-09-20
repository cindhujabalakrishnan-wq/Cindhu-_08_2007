package com.insurance.platform.repository;

import com.insurance.platform.model.entity.User;
import com.insurance.platform.model.enums.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Persistence access for {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique email address.
     *
     * @param email login email
     * @return matching user, if any
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether an email address is already registered.
     *
     * @param email login email
     * @return true when a user with the email exists
     */
    boolean existsByEmail(String email);

    /**
     * Lists all users holding the given role.
     *
     * @param role security role
     * @return users with the role
     */
    List<User> findByRole(Role role);

    /**
     * Searches users by email, first name or last name (case-insensitive).
     *
     * @param email search fragment matched against email
     * @param firstName search fragment matched against first name
     * @param lastName search fragment matched against last name
     * @param pageable pagination
     * @return matching users
     */
    org.springframework.data.domain.Page<User>
            findByEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    String email, String firstName, String lastName,
                    org.springframework.data.domain.Pageable pageable);
}
