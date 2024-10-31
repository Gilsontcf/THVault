package com.vault.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.vault.service.FileService;

/**
 * Kafka consumer to process file data asynchronously.
 */
@Service
public class FileUploadConsumer {

	@Autowired
	private FileService fileService;

	/**
	 * Listens to file upload messages and processes them.
	 *
	 * @param fileId   The ID of the file to process.
	 * @param fileData The byte array of file data.
	 * @throws Exception
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
