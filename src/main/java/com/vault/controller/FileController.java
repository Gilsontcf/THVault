package com.vault.controller;

import com.vault.model.File;
import com.vault.model.FileChunk;
import com.vault.model.User;
import com.vault.security.CustomUserPrincipal;
import com.vault.service.FileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {
	@Autowired
	private FileService fileService;

	@PostMapping
	public ResponseEntity<File> addFile(@RequestParam("file") MultipartFile file,
			@RequestParam("description") String description, @AuthenticationPrincipal CustomUserPrincipal userPrincipal)
			throws IOException {

		User user = userPrincipal.getUser();
		File newFile = new File();
		newFile.setName(file.getOriginalFilename());
		newFile.setDescription(description);
		newFile.setFileType(file.getContentType());
		newFile.setFileSize(file.getSize());

		// Split file into chunks
		List<byte[]> chunks = splitFileIntoChunks(file.getBytes());

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
	public ResponseEntity<byte[]> downloadFile(@PathVariable Long id, @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
	    User user = userPrincipal.getUser();

	    List<FileChunk> chunks = fileService.getFileChunks(id, user);
	    if (chunks.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	    }

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    try {
	        for (FileChunk chunk : chunks) {
	            outputStream.write(chunk.getChunk());
	        }
	    } catch (IOException e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }

	    byte[] fileContent = outputStream.toByteArray();
	    File file = fileService.getFileById(id, user)
                .orElseThrow(() -> new RuntimeException("File not found"));
	    return ResponseEntity.ok()
	            .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
	            .contentType(MediaType.APPLICATION_OCTET_STREAM)
	            .body(fileContent);
	}
	

//	@GetMapping("/{id}")
//	public ResponseEntity<File> getFileInfo(@PathVariable Long id,
//			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
//
//		User user = userPrincipal.getUser();
//		return fileService.getFileById(id, user).map(ResponseEntity::ok)
//				.orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
//	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteFile(@PathVariable Long id,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {

		User user = userPrincipal.getUser();
		fileService.deleteFile(id, user);
		return ResponseEntity.ok("File deleted successfully");
	}

	private List<byte[]> splitFileIntoChunks(byte[] fileData) {
		int chunkSize = 1024 * 1024; // 1MB chunks
		List<byte[]> chunks = new ArrayList<>();
		for (int i = 0; i < fileData.length; i += chunkSize) {
			int end = Math.min(fileData.length, i + chunkSize);
			chunks.add(Arrays.copyOfRange(fileData, i, end));
		}
		return chunks;
	}
}
