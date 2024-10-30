package com.vault.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vault.model.File;
import com.vault.model.FileChunk;
import com.vault.model.User;
import com.vault.repository.FileChunkRepository;
import com.vault.repository.FileRepository;
import com.vault.util.FileEncryptionUtil;

/**
 * Service for handling file operations: save, download, update, delete.
 */
@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileChunkRepository fileChunkRepository;

    private final FileEncryptionUtil fileEncryptionUtil;

    @Autowired
    public FileService(@Value("${app.security.aes-file-key}") String aesKey) {
        this.fileEncryptionUtil = new FileEncryptionUtil(aesKey);
    }

    public File saveFile(File file, List<byte[]> chunks, User user) throws Exception {
        file.setUser(user);
        file.setCreatedAt(LocalDateTime.now());
        file.setUpdatedAt(LocalDateTime.now());
        fileRepository.save(file);

        if (chunks != null && !chunks.isEmpty()) {
            int order = 0;
            for (byte[] chunk : chunks) {
                if (chunk != null) {
                    byte[] encryptedChunk = fileEncryptionUtil.encrypt(chunk);
                    FileChunk fileChunk = new FileChunk();
                    fileChunk.setFile(file);
                    fileChunk.setChunk(encryptedChunk);
                    fileChunk.setChunkOrder(order++);
                    fileChunkRepository.save(fileChunk);
                }
            }
        } else {
            throw new IllegalArgumentException("Chunks cannot be null or empty");
        }

        return file;
    }

    @Transactional(readOnly = true)
    public byte[] downloadFile(Long fileId, User user) throws Exception {
        List<FileChunk> chunks = fileChunkRepository.findByFileIdOrderByChunkOrderAsc(fileId);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (FileChunk chunk : chunks) {
            byte[] decryptedChunk = fileEncryptionUtil.decrypt(chunk.getChunk());
            outputStream.write(decryptedChunk);
        }
        return outputStream.toByteArray();
    }
    
    public File getFileById(Long fileId, User user) {
        File file = fileRepository.findByIdAndUserId(fileId, user.getId());
        if (file != null) {
            return file;
        }
        return null;
    }

    @Transactional
    public void deleteFile(Long fileId, User user) {
        File file = getFileById(fileId, user);
        if (file != null) {
            fileChunkRepository.deleteByFileId(fileId);
            fileRepository.delete(file);
        }
    }

    public File updateFileMetadata(Long fileId, String newName, String newDescription, User user) {
        File existingFile = getFileById(fileId, user);
        if (existingFile != null) {
            existingFile.setName(newName);
            existingFile.setDescription(newDescription);
            existingFile.setUpdatedAt(LocalDateTime.now());
            return fileRepository.save(existingFile);
        }
        throw new RuntimeException("File not found or access denied");
    }
}
