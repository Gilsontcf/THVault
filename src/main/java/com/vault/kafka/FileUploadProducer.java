package com.vault.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka producer for sending file chunks asynchronously to Kafka for processing.
 * Each chunk is wrapped in a FileChunkMessage object before being sent.
 */
@Service
public class FileUploadProducer {

    private static final String TOPIC = "file-uploads";

    @Autowired
    private KafkaTemplate<String, FileChunkMessage> kafkaTemplate;

    /**
     * Sends a file chunk message to Kafka for asynchronous processing.
     * @param fileId The file ID to associate with this chunk.
     * @param chunkOrder The order of the chunk to maintain sequence.
     * @param chunkData The byte array data of the chunk.
     */
    public void sendFileChunk(Long fileId, int chunkOrder, byte[] chunkData) {
        FileChunkMessage chunkMessage = new FileChunkMessage(fileId, chunkOrder, chunkData);
        kafkaTemplate.send(TOPIC, String.valueOf(fileId), chunkMessage);
    }
}
