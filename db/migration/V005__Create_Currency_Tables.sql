-- V005: Create currencies and currency_translations tables

CREATE TABLE currencies (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    symbol VARCHAR(10) NOT NULL,
    rate_to_vnd DECIMAL(20, 6) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM'
);

CREATE TABLE currency_translations (
    id BIGSERIAL PRIMARY KEY,
    currency_id BIGINT NOT NULL REFERENCES currencies(id) ON DELETE CASCADE,
    locale VARCHAR(10) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM',
    UNIQUE(currency_id, locale)
);

CREATE INDEX idx_currency_translations_currency_id ON currency_translations(currency_id);
