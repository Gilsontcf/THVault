package com.vault.repository;

import com.vault.model.FileChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileChunkRepository extends JpaRepository<FileChunk, Long> {
    List<FileChunk> findByFileIdOrderByChunkOrderAsc(Long fileId);
}
