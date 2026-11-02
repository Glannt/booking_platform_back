-- Migration for ServiceType and ServiceTypeTranslation tables

-- Create service_types table
CREATE TABLE IF NOT EXISTS service_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    icon VARCHAR(255),
    display_order INT DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM'
);

-- Create service_type_translations table
CREATE TABLE IF NOT EXISTS service_type_translations (
    id BIGSERIAL PRIMARY KEY,
    service_type_id BIGINT NOT NULL,
    locale VARCHAR(10) NOT NULL,
    name VARCHAR(255) NOT NULL,
    seo_title VARCHAR(255),
    seo_description VARCHAR(1000),
    canonical_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM',
    CONSTRAINT fk_service_type_id FOREIGN KEY (service_type_id) REFERENCES service_types(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_service_type_code ON service_types(code);
CREATE INDEX idx_service_type_active ON service_types(active);
CREATE INDEX idx_service_type_display_order ON service_types(display_order);
CREATE INDEX idx_service_type_translation_locale ON service_type_translations(locale);
CREATE INDEX idx_service_type_translation_service_id ON service_type_translations(service_type_id);

