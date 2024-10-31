package com.vault.model;

import javax.validation.constraints.NotNull;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Entity representing a chunk of a file for storage and processing.
 * Each chunk is part of a larger file and is stored with an order to reassemble the file.
 */
@Entity
public class FileChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private byte[] chunk;

    @NotNull
    private Integer chunkOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public byte[] getChunk() {
		return chunk;
	}

	public void setChunk(byte[] chunk) {
		this.chunk = chunk;
	}

	public Integer getChunkOrder() {
		return chunkOrder;
	}

	public void setChunkOrder(Integer chunkOrder) {
		this.chunkOrder = chunkOrder;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

}
