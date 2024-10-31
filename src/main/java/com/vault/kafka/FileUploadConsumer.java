package com.vault.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.vault.service.FileService;

/**
 * Kafka consumer for processing file chunks asynchronously.
 * Listens to Kafka topic and handles each chunk individually.
 */
@Service
public class FileUploadConsumer {

	@Autowired
	private FileService fileService;

	/**
     * Consumes a file chunk message from Kafka and processes it.
     * Updates file status to 'completed' upon successful processing.
     * If an error occurs, the status is updated with an error message.
     */
	@KafkaListener(topics = "file-uploads", groupId = "file-upload-group")
	public void consumeFileChunk(FileChunkMessage chunkMessage) {
		try {
			fileService.processFileChunk(chunkMessage.getFileId(), chunkMessage.getChunkOrder(),
					chunkMessage.getChunkData());
			fileService.updateFileStatus(chunkMessage.getFileId(), "completed", null);
		} catch (Exception e) {
			fileService.updateFileStatus(chunkMessage.getFileId(), "error", e.getMessage());
			System.err.println("Error processing file with ID " + chunkMessage.getFileId() + ": " + e.getMessage());
		}
	}

}
