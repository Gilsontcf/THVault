package com.vault.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.vault.model.File;
import com.vault.model.User;
import com.vault.service.CustomUserPrincipal;
import com.vault.service.FileService;

/**
 * REST Controller for managing file operations: upload, download, update, and delete.
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * Uploads a new file, chunking and saving it to the database.
     */
//    @PostMapping
//    public ResponseEntity<File> addFile(@RequestParam("file") MultipartFile file,
//                                        @RequestParam("description") String description,
//                                        @AuthenticationPrincipal CustomUserPrincipal userPrincipal) throws IOException, Exception {
//        User user = userPrincipal.getUser();
//        File newFile = new File();
//        newFile.setName(file.getOriginalFilename());
//        newFile.setDescription(description);
//        newFile.setFileType(file.getContentType());
//        newFile.setFileSize(file.getSize());
//
//        List<byte[]> chunks = splitFileIntoChunks(file.getInputStream());
//        File savedFile = fileService.saveFile(newFile, chunks, user);
//        return new ResponseEntity<>(savedFile, HttpStatus.CREATED);
//    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> addFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String description,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) throws Exception {
        
        User user = userPrincipal.getUser();
        File newFile = new File();
        newFile.setName(file.getOriginalFilename());
        newFile.setDescription(description);
        newFile.setFileType(file.getContentType());
        newFile.setFileSize(file.getSize());
        
        List<byte[]> chunks = splitFileIntoChunks(file.getInputStream());
        File savedFile = fileService.saveFile(newFile, chunks, user);

        // Construct response with metadata and file access URL
        Map<String, Object> response = new HashMap<>();
        response.put("name", savedFile.getName());
        response.put("description", savedFile.getDescription());
        response.put("fileType", savedFile.getFileType());
        response.put("fileSize", savedFile.getFileSize());
        response.put("fileUrl", "/api/files/" + savedFile.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(file);
    }

    /**
     * Endpoint to download a file by its ID, optimized for large files with streaming.
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) throws Exception {
        
        
        User user = userPrincipal.getUser();
        // Verify User/File
        File file = fileService.getFileById(id, user);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body(null);
        }
        
        byte[] decryptedFileContent = fileService.downloadFile(id, user);

        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(decryptedFileContent));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }

    /**
     * Deletes a file by its ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id,
                                             @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
    	
    	User user = userPrincipal.getUser();

        // Verify User/File
        File file = fileService.getFileById(id, user);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("You do not have permission to delete this file.");
        }

        fileService.deleteFile(id, user);
        return ResponseEntity.ok("File deleted successfully");
 
    }

    /**
     * Updates the file metadata (name and description).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateFileMetadata(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        File updatedFile = fileService.updateFileMetadata(id, name, description, user);

        // Construct response with updated metadata and file access URL
        Map<String, Object> response = new HashMap<>();
        response.put("name", updatedFile.getName());
        response.put("description", updatedFile.getDescription());
        response.put("fileType", updatedFile.getFileType());
        response.put("fileSize", updatedFile.getFileSize());
        response.put("fileUrl", "/api/files/" + updatedFile.getId());
        return ResponseEntity.ok(response);
    }

    private List<byte[]> splitFileIntoChunks(InputStream inputStream) throws IOException {
        int chunkSize = 1024 * 1024;
        List<byte[]> chunks = new ArrayList<>();
        byte[] buffer = new byte[chunkSize];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byte[] chunk = new byte[bytesRead];
            System.arraycopy(buffer, 0, chunk, 0, bytesRead);
            chunks.add(chunk);
        }
        return chunks;
    }
}
