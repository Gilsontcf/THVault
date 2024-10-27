package com.vault.model;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String username;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @Past
    private LocalDate dateOfBirth;

    @NotBlank
    private String passwordHash;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
}
