package com.vault.controller;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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

import com.vault.exception.ResourceNotFoundException;
import com.vault.exception.IllegalArgumentException;
import com.vault.kafka.FileUploadProducer;
import com.vault.model.File;
import com.vault.model.User;
import com.vault.service.CustomUserPrincipal;
import com.vault.service.FileService;

/**
 * REST Controller for managing file operations: upload, download, update, and
 * delete. This controller handles requests for file management and interacts
 * with Kafka for asynchronous processing.
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

	private static final String STATUS_PENDING = "pending";

	private static final String CHUNKS_SENT_ASYNCHRONOUS = "File chunks sent for asynchronous processing";

	private static final String PARAMETER_MUST_BE_PROVIDED = "At least one parameter must be provided to update.";

	private static final String FILE_NOT_FOUND = "File not found.";

	private static final String DELETE_SUCCESSFULL = "File deleted successfully";

	@Autowired
	private FileService fileService;

	@Autowired
	private FileUploadProducer fileUploadProducer;

	/**
	 * Test suite for FileController class.
	 * Tests upload, download, update, and delete functionalities for files.
	 */
	@PostMapping
	public ResponseEntity<Map<String, Object>> addFile(@RequestParam("file") MultipartFile file,
			@RequestParam("description") String description, @AuthenticationPrincipal CustomUserPrincipal userPrincipal)
			throws Exception {
		
		// Save initial file metadata with status 'pending'
		User user = userPrincipal.getUser();
		File newFile = new File();
		newFile.setName(file.getOriginalFilename());
		newFile.setDescription(description);
		newFile.setFileType(file.getContentType());
		newFile.setFileSize(file.getSize());
		newFile.setStatus(STATUS_PENDING);
		File savedFile = fileService.saveInitialFile(newFile, user);

		// Send file chunks to Kafka for asynchronous processing
		List<byte[]> chunks = fileService.splitFileIntoChunks(file.getBytes());

		for (int i = 0; i < chunks.size(); i++) {
			fileUploadProducer.sendFileChunk(savedFile.getId(), i, chunks.get(i));
		}

		Map<String, Object> response = new HashMap<>();
		response.put("status", CHUNKS_SENT_ASYNCHRONOUS);
		response.put("fileId", savedFile.getId());
		response.put("name", savedFile.getName());
		response.put("description", savedFile.getDescription());
		response.put("fileType", savedFile.getFileType());
		response.put("fileSize", savedFile.getFileSize());
		response.put("fileUrl", "/api/files/" + savedFile.getId());
		return ResponseEntity.ok(response);
	}

	/**
	 * Retrieves metadata for a specific file.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<File> getFileInfo(@PathVariable Long id,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		User user = userPrincipal.getUser();
		File file = fileService.getFileById(id, user);
		if (file == null) {
			throw new ResourceNotFoundException(FILE_NOT_FOUND);
		}
		return ResponseEntity.ok(file);
	}

	/**
	 * Endpoint to download a file by its ID, optimized for large files with
	 * streaming.
	 */
	@GetMapping("/{id}/download")
	public ResponseEntity<Resource> downloadFile(@PathVariable Long id,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) throws Exception {
		User user = userPrincipal.getUser();
		File file = fileService.getFileById(id, user);

		if (file == null) {
			throw new ResourceNotFoundException(FILE_NOT_FOUND);
		}

		// Using streaming to manage large files and avoid memory issues
		byte[] decryptedFileContent = fileService.downloadFile(id, user);
		InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(decryptedFileContent));

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
				.body(resource);
	}

	/**
	 * Deletes a file by its ID.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable Long id,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		User user = userPrincipal.getUser();
		File file = fileService.getFileById(id, user);

		if (file == null) {
			throw new ResourceNotFoundException(FILE_NOT_FOUND);
		}

		fileService.deleteFile(id, user);
		Map<String, Object> response = new HashMap<>();
		response.put("status", DELETE_SUCCESSFULL);
		response.put("fileSize", file.getFileSize());
		response.put("name", file.getName());
		return ResponseEntity.ok(response);
	}

	/**
	 * Updates the file metadata (name and description).
	 */
	@PutMapping("/{id}")
	public ResponseEntity<Map<String, Object>> updateFileMetadata(@PathVariable Long id,
			@RequestParam("name") String name, @RequestParam("description") String description,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {

		if (StringUtils.isAllEmpty(name, description)) {
			throw new IllegalArgumentException(PARAMETER_MUST_BE_PROVIDED);
		}

		User user = userPrincipal.getUser();

		File existingFile = fileService.getFileById(id, user);
		if (existingFile != null) {
			File updatedFile = fileService.updateFileMetadata(existingFile, name, description);
			// Construct response with updated metadata and file access URL
			Map<String, Object> response = new HashMap<>();
			response.put("name", updatedFile.getName());
			response.put("description", updatedFile.getDescription());
			response.put("fileType", updatedFile.getFileType());
			response.put("fileSize", updatedFile.getFileSize());
			response.put("fileUrl", "/api/files/" + updatedFile.getId());
			return ResponseEntity.ok(response);
		} else {
			throw new ResourceNotFoundException(FILE_NOT_FOUND);
		}
	}
}
