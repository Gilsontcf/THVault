-- Create Database and User
CREATE DATABASE vault_db
    WITH OWNER = vault_user
    ENCODING = 'UTF8'
	LC_COLLATE = 'Portuguese_Portugal.1252'
	LC_CTYPE = 'Portuguese_Portugal.1252'
    CONNECTION LIMIT = -1
    TEMPLATE template0;
	
CREATE USER vault_user WITH PASSWORD 'securePassword';

GRANT CONNECT ON DATABASE vault_db TO vault_user;

-- Conexão com o banco de dados
\c vault_db;

-- Create a dedicated schema for organizing tables
CREATE SCHEMA IF NOT EXISTS vault AUTHORIZATION vault_user;

-- Set the default search path to include the vault schema
ALTER ROLE vault_user SET search_path TO vault;

-- Grant usage permissions on the schema
GRANT USAGE ON SCHEMA vault TO vault_user;


-- Criação das Tabelas
CREATE TABLE vault.users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    date_of_birth DATE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN vault.users.id IS 'Unique identifier for each user';
COMMENT ON COLUMN vault.users.username IS 'Unique username of the user';
COMMENT ON COLUMN vault.users.name IS 'Full name of the user';
COMMENT ON COLUMN vault.users.email IS 'Email address of the user';
COMMENT ON COLUMN vault.users.date_of_birth IS 'Date of birth of the user';
COMMENT ON COLUMN vault.users.password_hash IS 'Hashed password for user authentication';
COMMENT ON COLUMN vault.users.created_at IS 'Timestamp of when the user was created';
COMMENT ON COLUMN vault.users.updated_at IS 'Timestamp of the last update of the user information';

-- Indexes for optimized queries
CREATE INDEX idx_users_username ON vault.users(username);
CREATE INDEX idx_users_email ON vault.users(email);

CREATE TABLE vault.file (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    file_type VARCHAR(50) NOT NULL, -- Metadata for file type
    file_size BIGINT NOT NULL, -- Metadata for file size
    user_id INTEGER REFERENCES vault.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	status VARCHAR(20) DEFAULT 'pending' NOT NULL, -- Status of the file processing: pending, processing, completed, error
    error_message TEXT                             -- Error message if processing fails
);

COMMENT ON COLUMN vault.file.id IS 'Unique identifier for each file';
COMMENT ON COLUMN vault.file.name IS 'Original name of the file';
COMMENT ON COLUMN vault.file.description IS 'Description of the file';
COMMENT ON COLUMN vault.file.file_type IS 'MIME type of the file';
COMMENT ON COLUMN vault.file.file_size IS 'Size of the file in bytes';
COMMENT ON COLUMN vault.file.user_id IS 'ID of the user who owns the file';
COMMENT ON COLUMN vault.file.created_at IS 'Timestamp of when the file was uploaded';
COMMENT ON COLUMN vault.file.updated_at IS 'Timestamp of the last update of the file metadata';
COMMENT ON COLUMN vault.file.status IS 'Processing status of the file';
COMMENT ON COLUMN vault.file.error_message IS 'Error message if processing fails';

-- Index for file name and user_id for optimized queries
CREATE INDEX idx_file_name ON vault.file(name);
CREATE INDEX idx_file_user_id ON vault.file(user_id);

CREATE TABLE vault.file_chunk (
    id BIGSERIAL PRIMARY KEY,
    chunk BYTEA NOT NULL,
    chunk_order INT NOT NULL,
    file_id BIGINT REFERENCES vault.file(id) ON DELETE CASCADE
);

COMMENT ON COLUMN vault.file_chunk.id IS 'Unique identifier for each file chunk';
COMMENT ON COLUMN vault.file_chunk.chunk IS 'Binary data of the file chunk';
COMMENT ON COLUMN vault.file_chunk.chunk_order IS 'Order of the chunk in the file';
COMMENT ON COLUMN vault.file_chunk.file_id IS 'ID of the file to which this chunk belongs';

-- Index for file name and user_id for optimized queries
CREATE INDEX idx_file_chunk_order ON vault.file_chunk(chunk_order);
CREATE INDEX idx_file_file_id ON vault.file_chunk(file_id);

-- Permissions on the `users` table
GRANT SELECT, INSERT, UPDATE, DELETE ON vault.users TO vault_user;

-- Permissions on the `file` table
GRANT SELECT, INSERT, UPDATE, DELETE ON vault.file TO vault_user;

-- Permissions
GRANT ALL PRIVILEGES ON DATABASE vault_db TO vault_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO vault_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO vault_user;
GRANT ALL PRIVILEGES ON TABLE vault.users TO vault_user;
GRANT ALL PRIVILEGES ON SEQUENCE vault.users_id_seq TO vault_user;
GRANT ALL PRIVILEGES ON TABLE vault.file TO vault_user;
GRANT ALL PRIVILEGES ON SEQUENCE vault.file_id_seq TO vault_user;
GRANT ALL PRIVILEGES ON TABLE vault.file_chunk TO vault_user;
GRANT ALL PRIVILEGES ON SEQUENCE vault.file_chunk_id_seq TO vault_user;

-- Create user to test pass = password
INSERT INTO users (username, name, email, date_of_birth, password_hash, created_at, updated_at)
VALUES ('testuser', 'Test User', 'testuser@example.com', '1990-01-01', '$2a$10$sOnn6NaFSaBPh.HMYfyIde6xQdpDWetfJg5mfpXDUskBBGSwXZghi', now(), now());

INSERT INTO users (username, name, email, date_of_birth, password_hash, created_at, updated_at)
VALUES ('anothertestuser', 'Another Test User', 'anothertestuser@example.com', '1990-01-02', '$2a$10$sOnn6NaFSaBPh.HMYfyIde6xQdpDWetfJg5mfpXDUskBBGSwXZghi', now(), now());

