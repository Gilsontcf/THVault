package com.vault.repository;

import com.vault.model.File;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for File entity with custom methods.
 * Provides methods to fetch and manage File data associated with a specific user.
 */
@Repository
public interface FileRepository extends JpaRepository<File, Long> {

	/**
     * Finds a file by its ID and eagerly loads associated user data.
     * This method uses EntityGraph to fetch user information with the file details.
     * @param id The ID of the file.
     * @param userId The ID of the user associated with the file.
     * @return File object if found, otherwise null.
     */
    @EntityGraph(attributePaths = "user")  // Loads the associated User eagerly
    File findByIdAndUserId(Long id, Long userId);
}
