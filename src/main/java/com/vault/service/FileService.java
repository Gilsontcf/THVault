package com.vault.service;

import com.vault.model.File;
import com.vault.model.FileChunk;
import com.vault.model.User;
import com.vault.repository.FileChunkRepository;
import com.vault.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	public File saveFile(File file, List<byte[]> chunks, User user) {
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
                if (chunk != null) {  // Certifica-se de que cada chunk não seja nulo
                    FileChunk fileChunk = new FileChunk();
                    fileChunk.setFile(savedFile);
                    fileChunk.setChunk(chunk);  // Define o chunk atual
                    fileChunk.setChunkOrder(order++);
                    fileChunkRepository.save(fileChunk);
                }
            }
        } else {
            throw new IllegalArgumentException("Chunks cannot be null or empty");
        }

		return file;
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
}
