package com.vault.repository;

import com.vault.model.FileChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for FileChunk entity with methods for chunk ordering.
 * Provides methods to retrieve and delete file chunks associated with a specific file.
 */
@Repository
public interface FileChunkRepository extends JpaRepository<FileChunk, Long> {

	/**
     * Retrieves file chunks by file ID in ascending chunk order.
     * Used to reassemble the file in correct sequence.
     * @param fileId The ID of the file whose chunks are to be retrieved.
     * @return List of file chunks ordered by chunk order.
     */
    List<FileChunk> findByFileIdOrderByChunkOrderAsc(Long fileId);

    /**
     * Deletes all file chunks associated with a specific file ID.
     * Used for cleanup when a file is deleted.
     * @param fileId The ID of the file whose chunks are to be deleted.
     */
    void deleteByFileId(Long fileId);
}
