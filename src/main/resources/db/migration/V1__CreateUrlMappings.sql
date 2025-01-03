/**
 * Database schema for URL mappings.
 * Features:
 * - Unique constraints on short_id and url_hash
 * - TIMESTAMPTZ for proper timezone handling
 * - BYTEA for efficient hash storage
 */

 CREATE TABLE IF NOT EXISTS url_mappings (
    id BIGSERIAL PRIMARY KEY,
    short_id VARCHAR(255) NOT NULL,
    original_url TEXT NOT NULL,
    normalized_url TEXT NOT NULL,
    url_hash BYTEA NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

-- Create unique constraints
ALTER TABLE url_mappings
    ADD CONSTRAINT uk_short_id UNIQUE (short_id),
    ADD CONSTRAINT uk_url_hash UNIQUE (url_hash);
