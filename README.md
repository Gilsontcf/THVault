# THVault

# Vault Application

This is a secure RESTful API for storing and managing files, built with Java 8, Spring Boot, and PostgreSQL. The application supports file upload with chunking, file metadata, and user-specific permissions.

## Features

- **User Authentication**: Only logged-in users can add, retrieve, update, or delete files.
- **File Upload with Chunking**: Large files are split into chunks for efficient storage and management.
- **User-Specific File Access**: Users can only access files they uploaded.
- **File Metadata Support**: Includes file name, type, size, and additional description fields.

## Prerequisites

- Java 8
- PostgreSQL
- Maven

## Getting Started

### Database Setup

1. Start PostgreSQL and create a database user:
   ```sql
   CREATE USER vault_user WITH PASSWORD 'your_password';
   
Run the create_database.sql script to set up the database:
psql -U vault_user -f path/to/create_database.sql

Application Configuration
Edit the application.properties file in src/main/resources with your database credentials.

properties

spring.datasource.url=jdbc:postgresql://localhost:5432/vault_db
spring.datasource.username=vault_user
spring.datasource.password=your_password

Build and Run the Application
Use Maven to build the project:
mvn clean install

Run the application:
mvn spring-boot:run

The application will start on http://localhost:8081.

API Endpoints
User Authentication
Basic authentication with username and password is required for all endpoints.

File Management Endpoints
Upload File: POST /api/files

Uploads a file in chunks.
Requires file (MultipartFile) and description (String).
Get File Info: GET /api/files/{id}

Retrieves file metadata by file ID.
Get File Chunks: GET /api/files/{id}/chunks

Retrieves individual chunks for the file with the specified ID.
Download File: GET /api/files/{id}/download

Downloads the complete file by consolidating all chunks.
Delete File: DELETE /api/files/{id}

Deletes the specified file and returns a success message.
Testing the API
Use Postman or any other REST client to test the endpoints. For example, to upload a file, authenticate with your credentials and send a POST request with the file and description.

Logging
Logging is enabled for debugging purposes. You can adjust the log level in application.properties.

License
This project is licensed under the MIT License.
