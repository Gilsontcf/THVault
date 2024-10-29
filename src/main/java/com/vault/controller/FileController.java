package com.vault.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vault.model.File;
import com.vault.model.FileChunk;
import com.vault.model.User;
import com.vault.security.CustomUserPrincipal;
import com.vault.service.FileService;

@RestController
@RequestMapping("/api/files")
public class FileController {
	@Autowired
	private FileService fileService;

	@PostMapping
	public ResponseEntity<File> addFile(@RequestParam("file") MultipartFile file,
			@RequestParam("description") String description, @AuthenticationPrincipal CustomUserPrincipal userPrincipal)
			throws IOException, Exception {

		User user = userPrincipal.getUser();
		File newFile = new File();
		newFile.setName(file.getOriginalFilename());
		newFile.setDescription(description);
		newFile.setFileType(file.getContentType());
		newFile.setFileSize(file.getSize());

		// Split file into chunks
		List<byte[]> chunks = splitFileIntoChunks(file.getInputStream());

		File savedFile = fileService.saveFile(newFile, chunks, user);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedFile);
	}

	@GetMapping("/{id}/chunks")
	public ResponseEntity<List<FileChunk>> getFileChunks(@PathVariable Long id,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		User user = userPrincipal.getUser();

		List<FileChunk> chunks = fileService.getFileChunks(id, user);
		if (chunks.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		return ResponseEntity.ok(chunks);
	}

	@GetMapping("/{id}/download")
	public ResponseEntity<byte[]> downloadFile(@PathVariable Long id,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) throws Exception {
		User user = userPrincipal.getUser();

		byte[] decryptedFileContent = fileService.downloadFile(id, user);

		File file = fileService.getFileById(id, user).orElseThrow(() -> new RuntimeException("File not found"));
		return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
				.contentType(MediaType.APPLICATION_OCTET_STREAM).body(decryptedFileContent);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteFile(@PathVariable Long id,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {

		User user = userPrincipal.getUser();
		fileService.deleteFile(id, user);
		return ResponseEntity.ok("File deleted successfully");
	}

	@PutMapping("/{id}")
	public ResponseEntity<File> updateFile(@PathVariable Long id, @RequestParam("file") MultipartFile file,
			@RequestParam("description") String description, @AuthenticationPrincipal CustomUserPrincipal userPrincipal)
			throws Exception {

		User user = userPrincipal.getUser();

		File updatedFile = new File();
		updatedFile.setName(file.getOriginalFilename());
		updatedFile.setDescription(description);
		updatedFile.setFileType(file.getContentType());
		updatedFile.setFileSize(file.getSize());

		// Dividir e criptografar os novos chunks
		List<byte[]> newChunks = splitFileIntoChunks(file.getInputStream());

		// Atualizar o arquivo
		File savedFile = fileService.updateFile(id, updatedFile, newChunks, user);
		return ResponseEntity.ok(savedFile);
	}

	private List<byte[]> splitFileIntoChunks(InputStream inputStream) throws IOException {
		int chunkSize = 1024 * 1024;
		List<byte[]> chunks = new ArrayList<>();
		byte[] buffer = new byte[chunkSize];
		int bytesRead;

		while ((bytesRead = inputStream.read(buffer)) != -1) {
			byte[] chunk = Arrays.copyOf(buffer, bytesRead);
			chunks.add(chunk);
		}
		return chunks;
	}
}
