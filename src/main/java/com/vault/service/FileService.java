package com.vault.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

import io.micrometer.common.util.StringUtils;

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

	public File updateFileMetadata(File existingFile, String newName, String newDescription) {

		if (StringUtils.isNotEmpty(newName)) {
			existingFile.setName(newName);
		}
		if (StringUtils.isNotEmpty(newDescription)) {
			existingFile.setDescription(newDescription);
		}
		existingFile.setUpdatedAt(LocalDateTime.now());
		return fileRepository.save(existingFile);
	}

	public File saveInitialFile(File file, User user) {
		file.setUser(user);
		file.setCreatedAt(LocalDateTime.now());
		file.setStatus("pending");
		return fileRepository.save(file);
	}

	public void updateFileStatus(Long fileId, String status, String errorMessage) {
		File file = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File metadata not found"));
		file.setStatus(status);
		file.setUpdatedAt(LocalDateTime.now());
		file.setErrorMessage(errorMessage);
		fileRepository.save(file);
	}

	public List<byte[]> splitFileIntoChunks(byte[] fileData) throws IOException {
		int chunkSize = 512 * 512; // 512Kb
		List<byte[]> chunks = new ArrayList<>();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(fileData);
		byte[] buffer = new byte[chunkSize];
		int bytesRead;

		while ((bytesRead = inputStream.read(buffer)) != -1) {
			byte[] chunk = new byte[bytesRead];
			System.arraycopy(buffer, 0, chunk, 0, bytesRead);
			chunks.add(chunk);
		}

		return chunks;
	}

	/**
	 * Persists each chunk in the database as part of the file upload process.
	 *
	 * @param fileId     The ID of the file being processed.
	 * @param chunkOrder The order of the chunk in the file.
	 * @param chunkData  The byte array data of the chunk.
	 * @throws Exception
	 */
	public void processFileChunk(Long fileId, int chunkOrder, byte[] chunkData) throws Exception {
		File file = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File metadata not found"));

		// Save each chunk to the file_chunk table
		if (chunkData != null) {
			byte[] encryptedChunk = fileEncryptionUtil.encrypt(chunkData);
			FileChunk fileChunk = new FileChunk();
			fileChunk.setFile(file);
			fileChunk.setChunk(encryptedChunk);
			fileChunk.setChunkOrder(chunkOrder);
			fileChunkRepository.save(fileChunk);
		} else {
			throw new IllegalArgumentException("Chunks cannot be null or empty");
		}

	}
}
