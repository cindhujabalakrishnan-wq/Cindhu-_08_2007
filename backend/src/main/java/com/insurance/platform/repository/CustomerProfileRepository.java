package com.insurance.platform.repository;

import com.insurance.platform.model.entity.CustomerProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Persistence access for {@link CustomerProfile} entities.
 */
@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {

    /**
     * Finds the profile belonging to the given user.
     *
     * @param userId owning user id
     * @return matching profile, if any
     */
    Optional<CustomerProfile> findByUserId(Long userId);

    /**
     * Finds the profile by the owning user's email address.
     *
     * @param email user email
     * @return matching profile, if any
     */
    Optional<CustomerProfile> findByUserEmail(String email);

    /**
     * Checks whether the user already has a profile.
     *
     * @param userId owning user id
     * @return true when a profile exists
     */
    boolean existsByUserId(Long userId);
}
