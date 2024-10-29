package com.vault.service;

import com.vault.model.File;
import com.vault.model.FileChunk;
import com.vault.model.User;
import com.vault.repository.FileChunkRepository;
import com.vault.repository.FileRepository;
import com.vault.util.FileEncryptionUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

		// Salva o arquivo no repositório
		File savedFile = fileRepository.save(file);

		// Verifica se os chunks não estão vazios antes de salvar
		if (chunks != null && !chunks.isEmpty()) {
			int order = 0;
			for (byte[] chunk : chunks) {
				if (chunk != null) { // Certifica-se de que cada chunk não seja nulo
					byte[] encryptedChunk = fileEncryptionUtil.encrypt(chunk);
					FileChunk fileChunk = new FileChunk();
					fileChunk.setFile(savedFile);
					fileChunk.setChunk(encryptedChunk); // Armazena o chunk criptografado
					fileChunk.setChunkOrder(order++);
					fileChunkRepository.save(fileChunk);
				}
			}
		} else {
			throw new IllegalArgumentException("Chunks cannot be null or empty");
		}

		return file;
	}

	// Obter e descriptografar o arquivo para download
	@Transactional(readOnly = true)
	public byte[] downloadFile(Long fileId, User user) throws Exception {

		List<FileChunk> chunks = fileChunkRepository.findByFileIdOrderByChunkOrderAsc(fileId);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		for (FileChunk chunk : chunks) {
			byte[] decryptedChunk = fileEncryptionUtil.decrypt(chunk.getChunk()); // Descriptografa o chunk
			outputStream.write(decryptedChunk);
		}
		return outputStream.toByteArray();
	}

	public Optional<File> getFileById(Long fileId, User user) {
		return fileRepository.findById(fileId)
				.filter(f -> f.getUser() != null && f.getUser().getId().equals(user.getId()));
	}

	@Transactional(readOnly = true)
	public List<FileChunk> getFileChunks(Long fileId, User user) {
		return fileChunkRepository.findByFileIdOrderByChunkOrderAsc(fileId).stream()
				.filter(chunk -> chunk.getFile().getUser().getId().equals(user.getId())).collect(Collectors.toList());
	}

	@Transactional(readOnly = false)
	public void deleteFile(Long fileId, User user) {
		getFileById(fileId, user).ifPresent(file -> {
			fileChunkRepository.deleteAll(fileChunkRepository.findByFileIdOrderByChunkOrderAsc(fileId));
			fileRepository.delete(file);
		});
	}
	
	@Transactional(readOnly = false)
	public File updateFile(Long fileId, File updatedFile, List<byte[]> newChunks, User user) throws Exception {
        File existingFile = fileRepository.findById(fileId)
                .filter(f -> f.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("File not found or access denied"));

        // Atualize os metadados
        existingFile.setName(updatedFile.getName());
        existingFile.setDescription(updatedFile.getDescription());
        existingFile.setFileType(updatedFile.getFileType());
        existingFile.setFileSize(updatedFile.getFileSize());
        existingFile.setUpdatedAt(LocalDateTime.now());

        // Remover chunks antigos e salvar novos chunks criptografados
        fileChunkRepository.deleteAll(fileChunkRepository.findByFileIdOrderByChunkOrderAsc(fileId));
        int order = 0;
        for (byte[] chunk : newChunks) {
            byte[] encryptedChunk = fileEncryptionUtil.encrypt(chunk);
            FileChunk fileChunk = new FileChunk();
            fileChunk.setFile(existingFile);
            fileChunk.setChunk(encryptedChunk); // Salvar chunks criptografados
            fileChunk.setChunkOrder(order++);
            fileChunkRepository.save(fileChunk);
        }

        return fileRepository.save(existingFile);
    }
}
