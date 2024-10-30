package com.vault.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

/**
 * Entity representing a chunk of a file for storage and processing.
 */
@Entity
public class FileChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Type(type="org.hibernate.type.BinaryType")
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
