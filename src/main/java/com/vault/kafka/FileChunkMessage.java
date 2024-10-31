package com.vault.kafka;

import java.io.Serializable;

/**
 * Message object used for sending file chunks via Kafka.
 * Each message includes file ID, chunk order, and chunk data.
 */
public class FileChunkMessage implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3940012355178521964L;
	
	private Long fileId;
	private int chunkOrder;
	private byte[] chunkData;
	
	// Standart Constructor requested by Json
    public FileChunkMessage() {
    }

	public FileChunkMessage(Long fileId, int chunkOrder, byte[] chunkData) {
		this.fileId = fileId;
		this.chunkOrder = chunkOrder;
		this.chunkData = chunkData;
	}

	public Long getFileId() {
		return fileId;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}

	public int getChunkOrder() {
		return chunkOrder;
	}

	public void setChunkOrder(int chunkOrder) {
		this.chunkOrder = chunkOrder;
	}

	public byte[] getChunkData() {
		return chunkData;
	}

	public void setChunkData(byte[] chunkData) {
		this.chunkData = chunkData;
	}

}
