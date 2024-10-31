package com.vault.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka producer to send file upload data asynchronously for processing.
 */
@Service
public class FileUploadProducer {

    private static final String TOPIC = "file-uploads";

    @Autowired
    private KafkaTemplate<String, FileChunkMessage> kafkaTemplate;

    /**
     * Sends file data to Kafka for asynchronous processing.
     *
     * @param fileId The file ID for tracking.
     * @param fileData The byte array of file data.
     */
    public void sendFileChunk(Long fileId, int chunkOrder, byte[] chunkData) {
        FileChunkMessage chunkMessage = new FileChunkMessage(fileId, chunkOrder, chunkData);
        kafkaTemplate.send(TOPIC, String.valueOf(fileId), chunkMessage);
    }
}
