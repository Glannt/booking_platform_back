-- This is a reference SQL migration for MasterEntity and SINGLE_TABLE inheritance strategy
-- Actual migrations should be managed by Liquibase or Flyway
-- Note: Timestamps use TIMESTAMP type which maps to LocalDateTime in Java

-- Base table for SINGLE_TABLE inheritance strategy
-- All entities extending MasterEntity will use this table with entity_type discriminator
CREATE TABLE master_entity (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(31) NOT NULL,  -- Discriminator column for SINGLE_TABLE strategy
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM',

    -- Menu entity fields
    name VARCHAR(255),
    description VARCHAR(500),
    price DECIMAL(10, 2),
    is_active BOOLEAN DEFAULT TRUE,

    -- Timestamps for auditing
    CREATED_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) PARTITION BY RANGE (created_at);

-- Create indexes for better query performance
CREATE INDEX idx_entity_type ON master_entity(entity_type);
CREATE INDEX idx_created_at ON master_entity(created_at);
CREATE INDEX idx_deleted_at ON master_entity(deleted_at);
CREATE INDEX idx_created_by ON master_entity(created_by);

-- Example: If using separate tables instead (TABLE_PER_CLASS strategy) - NOT RECOMMENDED
-- CREATE TABLE menu (
--     id BIGSERIAL PRIMARY KEY,
--     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     deleted_at TIMESTAMP,
--     created_by VARCHAR(255) DEFAULT 'SYSTEM',
--     name VARCHAR(255) NOT NULL,
--     description VARCHAR(500),
--     price DECIMAL(10, 2),
--     is_active BOOLEAN DEFAULT TRUE
-- );

-- Note on SINGLE_TABLE strategy:
-- Pros:
-- - Single query across all entity types
-- - Better join performance
-- - Simpler database schema
--
-- Cons:
-- - Single table can become large
-- - More nullable columns for specialized entity fields
-- - Requires discriminator column
--
-- For modulith with multiple entities, SINGLE_TABLE is recommended for its simplicity
-- and performance characteristics when querying across modules.

