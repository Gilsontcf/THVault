package com.vault.repository;

import com.vault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for User entity with methods for finding users by username.
 * Provides a way to load User data needed for authentication.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	 /**
     * Finds a user by username.
     * This is used by authentication services to load user details.
     * @param username The username of the user to find.
     * @return User object if found, otherwise null.
     */
    User findByUsername(String username);
}
