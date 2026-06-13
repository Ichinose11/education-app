-- DDL SQL Script: Initialize user_db and users table
-- Target Database: PostgreSQL

-- 1. Create database (Optional/Reference)
-- To execute this manually:
-- CREATE DATABASE user_db;
-- \c user_db;

-- 2. Enable extensions
-- Enables UUID generation function: gen_random_uuid() (standard since PostgreSQL 13)
-- For older versions, we can use "uuid-ossp" extension.
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 3. Create Users Table
CREATE TABLE IF NOT EXISTS users (
    -- id can be SERIAL or UUID. We use UUID for better scalability and security.
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Username: 3 to 50 characters, unique and required
    username VARCHAR(50) UNIQUE NOT NULL,
    
    -- Password: Salted and hashed string (e.g., bcrypt, argon2). Required.
    password VARCHAR(255) NOT NULL,
    
    -- Email: Standard format, unique and required
    email VARCHAR(255) UNIQUE NOT NULL,
    
    -- Role: Controls access permissions. Defaults to 'USER'.
    role VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('ADMIN', 'USER', 'MODERATOR')),
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Create Indexes
-- Indexes for columns frequently used in WHERE, JOIN, and UNIQUE checks to optimize lookup speed.
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- 5. Automatically update updated_at on modification
-- Function to set updated_at column to current timestamp
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to execute the set_updated_at function before any UPDATE query
DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
