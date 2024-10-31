package com.vault.repository;

import com.vault.model.File;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for File entity with custom methods.
 */
@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    /**
     * Finds a file by its ID and eagerly loads associated user data.
     */
    @EntityGraph(attributePaths = "user")  // Loads the associated User eagerly
    File findByIdAndUserId(Long id, Long userId);
}
