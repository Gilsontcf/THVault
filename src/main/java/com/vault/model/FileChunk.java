package com.vault.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class FileChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @NotNull
    private byte[] chunk;

    @NotNull
    private Integer chunkOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    // Getters and Setters
}
