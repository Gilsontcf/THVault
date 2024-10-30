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
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    date_of_birth DATE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for optimized queries
CREATE INDEX idx_users_username ON vault.users(username);
CREATE INDEX idx_users_email ON vault.users(email);

CREATE TABLE vault.file (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    file_type VARCHAR(50) NOT NULL, -- Metadata for file type
    file_size BIGINT NOT NULL, -- Metadata for file size
    chunk BYTEA NOT NULL, -- File data stored in chunks
    chunk_order INTEGER NOT NULL, -- Order of the file chunk
    user_id INTEGER REFERENCES vault.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for file name and user_id for optimized queries
CREATE INDEX idx_file_name ON vault.file(name);
CREATE INDEX idx_file_user_id ON vault.file(user_id);

CREATE TABLE vault.file_chunk (
    id BIGSERIAL PRIMARY KEY,
    chunk BYTEA NOT NULL,
    chunk_order INT NOT NULL,
    file_id BIGINT REFERENCES vault.file(id) ON DELETE CASCADE
);

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

-- Create user to test pass = password
INSERT INTO users (username, name, email, date_of_birth, password_hash, created_at, updated_at)
VALUES ('testuser', 'Test User', 'testuser@example.com', '1990-01-01', '$2a$10$sOnn6NaFSaBPh.HMYfyIde6xQdpDWetfJg5mfpXDUskBBGSwXZghi', now(), now());

INSERT INTO users (username, name, email, date_of_birth, password_hash, created_at, updated_at)
VALUES ('anothertestuser', 'Another Test User', 'anothertestuser@example.com', '1990-01-02', '$2a$10$sOnn6NaFSaBPh.HMYfyIde6xQdpDWetfJg5mfpXDUskBBGSwXZghi', now(), now());

