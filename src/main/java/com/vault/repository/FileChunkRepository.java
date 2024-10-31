package com.vault.repository;

import com.vault.model.FileChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for FileChunk entity with methods for chunk ordering.
 */
@Repository
public interface FileChunkRepository extends JpaRepository<FileChunk, Long> {

    /**
     * Retrieves file chunks by file ID in ascending chunk order.
     */
    List<FileChunk> findByFileIdOrderByChunkOrderAsc(Long fileId);

    /**
     * Deletes all file chunks associated with a specific file ID.
     */
    void deleteByFileId(Long fileId);
}
