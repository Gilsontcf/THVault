
# Vault API

This project is a file management API built in Java, using Spring Boot and Kafka for asynchronous uploads. The API enables operations such as file upload, download, update, and deletion. It also provides chunk encryption, access control, and optimizations for high load.

## Features

- **File Upload and Download**: Supports file storage in chunks to facilitate transmission and retrieval.
- **Asynchronous Upload with Kafka**: Kafka is used to manage the upload of large files asynchronously, splitting the process into chunks to prevent overloading.
- **AES Encryption**: All file chunks are encrypted using AES before storage for added security.
- **Access Control with Spring Security**: Protects endpoints with role-based authentication and authorization.
- **PostgreSQL Performance Configuration**: Cache adjustments and optimizations to support high loads.

## Architecture and Design

The project follows the **MVC** (Model-View-Controller) architecture to organize control logic, data handling, and user interactions. It also leverages a **microservices** pattern with Kafka to ensure scalability in the asynchronous processing of large files, distributing the load across services.

### Key Components

- **Controller Layer**: Manages API requests.
- **Service Layer**: Processes business logic, including encryption and chunk handling.
- **Repository Layer**: Communicates with the PostgreSQL database.
- **Kafka Producer/Consumer**: Handles sending and consuming chunk messages for asynchronous processing.

## Setup and Execution

### Prerequisites

- **Java 17**
- **Maven**
- **PostgreSQL**
- **Kafka**

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Gilsontcf/THVault.git
   ```

2. **Configure PostgreSQL**:
   - Create a database for the project.
   - Adjust the PostgreSQL connection credentials in `application.properties` with the correct username, password, and database name.

3. **Configure Kafka**:
   - Install and start Kafka on a local or remote server.
   - In `application.properties`, set the Kafka server address:
     ```properties
     spring.kafka.bootstrap-servers=localhost:9092
     ```

4. **Set up AES Key**:
   - In `application.properties`, configure the AES key for file encryption:
     ```properties
     app.security.aes-file-key=<your-key-here>
     ```

5. **Build and start the application**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## API Endpoints

### File Upload

```http
POST /api/files
```

**Description**: Initiates file upload in chunks asynchronously.

**Parameters**:
- `file`: The file to be uploaded.
- `description`: Description of the file.

**Response**:
- URL to check the file status.

### File Download

```http
GET /api/files/{id}/download
```

**Description**: Downloads a file by its ID.

### Update File Metadata

```http
PUT /api/files/{id}
```

**Description**: Updates file metadata (name and description).

**Parameters**:
- `name`: New name for the file.
- `description`: New description for the file.

### Delete File

```http
DELETE /api/files/{id}
```

**Description**: Deletes the file specified by the ID.

## Security

Security is managed by **Spring Security**, configuring role-based authentication. To access protected endpoints, HTTP Basic authentication is required, and the user’s role must be `USER`.

### Security Configuration

- **CSRF Disabled**: CSRF protection has been disabled for stateless REST API usage.
- **HTTP Basic Authentication**: Enabled to secure API endpoints.

## Kafka and Asynchronous Uploads

### Configuration

Kafka is used to manage asynchronous file uploads, processing each chunk separately. The `FileUploadProducer` sends file chunks to the `file-uploads` topic, while the `FileUploadConsumer` processes them for storage.

### `application.properties` Configuration

```properties
# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=file-upload-group
spring.kafka.consumer.auto-offset-reset=earliest
```

## PostgreSQL Performance Optimization

To improve performance under high loads, several optimizations have been applied in PostgreSQL:

- **Query Cache Adjustments**: Configured to maximize query caching.
- **Optimized Queries**: Analysis and optimization of read/write queries to reduce response time in frequent operations.
- **`work_mem` and `maintenance_work_mem` Adjustments**: Set to better handle sorting and joining operations in complex queries.

## Testing

The project uses **JUnit** and **Mockito** for test coverage, including unit tests for services and controllers, as well as integration tests with `MockMvc`.

To run the tests:

```bash
mvn test
```

## Package Structure

The package structure is organized according to the MVC architecture, separating responsibilities across controllers, services, and repositories:

- **controller**: Contains REST endpoints.
- **service**: Contains business logic and Kafka integration.
- **repository**: Interface to the PostgreSQL database.
- **kafka**: Contains the Kafka `Producer` and `Consumer` for file upload.
- **config**: Security and Kafka configurations.

## License

This project is licensed under the MIT License.

---

This README provides an overview of the project, architecture, setup, usage, and installation instructions. It can be expanded further if needed to include response examples and additional configuration details.
