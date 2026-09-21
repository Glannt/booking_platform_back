-- =====================================================================
-- V013__Rename_Rental_To_Self_Drive_And_Create_Price_Discount.sql
--
-- 1. Rename rental tables to self_drive
-- 2. Create price_discounts table for cross-service discounts
-- =====================================================================

-- 1. Rename rental tables
ALTER TABLE IF EXISTS rentals RENAME TO self_drive_rentals;
ALTER TABLE IF EXISTS rental_prices RENAME TO self_drive_rental_prices;
ALTER TABLE IF EXISTS rental_types RENAME TO self_drive_types;
ALTER TABLE IF EXISTS rental_type_translations RENAME TO self_drive_type_translations;

-- 2. Create price_discounts table
CREATE TABLE IF NOT EXISTS price_discounts (
    id SERIAL PRIMARY KEY,
    price_list_id INT REFERENCES price_lists(id) ON DELETE SET NULL,
    price_type_id INT REFERENCES price_types(id) ON DELETE SET NULL,
    scope_type VARCHAR(50),                     -- 'INTERCITY_ROUTE', 'SHARED_ROUTE', 'SELF_DRIVE', 'TOUR', 'AIRPORT_TRANSFER'
    scope_ref_id VARCHAR(100),
    code VARCHAR(50),                           -- promo code (if any)
    discount_type VARCHAR(20) NOT NULL,         -- 'PERCENT' | 'FIXED_AMOUNT'
    discount_value NUMERIC(14, 4) NOT NULL,     -- 10.00 (%) or 200000 (VND)
    min_days INT,                               -- e.g. min 3 rental days
    min_order_amount NUMERIC(14, 2),            -- min order value
    customer_segment VARCHAR(50),               -- 'VIP', 'NEW_CUSTOMER', etc.
    valid_from TIMESTAMP NOT NULL DEFAULT now(),
    valid_to TIMESTAMP,
    max_usage INT,
    usage_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM'
);

CREATE INDEX IF NOT EXISTS idx_price_discount_lookup ON price_discounts(code, status, valid_from, valid_to);
CREATE INDEX IF NOT EXISTS idx_price_discount_scope ON price_discounts(scope_type, scope_ref_id, status);
