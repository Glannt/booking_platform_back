-- =====================================================================
-- V010__Create_Price_List_And_Intercity_Pricing.sql
--
-- Implements price_table_implement_intercity.md:
--   * price_types (ONE_WAY, ROUND_TRIP_SAME_DAY, DAILY_RATE)
--   * price_lists (scope_type = 'INTERCITY_ROUTE')
--   * price_list_translations
--   * price_list_routes
--   * price_list_daily_rates
-- Cleans up obsolete/incorrect price data in intercity_route_prices.
-- =====================================================================

-- 1. Create price_types table
CREATE TABLE IF NOT EXISTS price_types (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(30) NOT NULL,
    default_name VARCHAR(100) NOT NULL,
    description TEXT,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM'
);

-- Insert standard price types
INSERT INTO price_types (code, category, default_name, description) VALUES
('ONE_WAY', 'IN_DAY', 'Một chiều', 'Chuyến xe một chiều theo tuyến cố định'),
('ROUND_TRIP_SAME_DAY', 'IN_DAY', 'Khứ hồi trong ngày', 'Chuyến xe đi và về trong cùng ngày'),
('DAILY_RATE', 'DAILY_RATE', 'Giá theo ngày', 'Thuê xe có tài xế theo ngày (nhân theo số ngày)')
ON CONFLICT (code) DO NOTHING;

-- 2. Create price_lists table
CREATE TABLE IF NOT EXISTS price_lists (
    id SERIAL PRIMARY KEY,
    price_type_id INT NOT NULL REFERENCES price_types(id),
    scope_type VARCHAR(50) NOT NULL DEFAULT 'INTERCITY_ROUTE',
    scope_ref_id VARCHAR(100) NOT NULL,
    scope_meta JSONB,
    vehicle_category_id BIGINT,
    currency VARCHAR(10) DEFAULT 'VND',
    base_price NUMERIC(15, 2) NOT NULL,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM'
);

CREATE INDEX IF NOT EXISTS idx_price_lists_scope ON price_lists(scope_type, scope_ref_id, vehicle_category_id, status);

-- 3. Create price_list_translations table
CREATE TABLE IF NOT EXISTS price_list_translations (
    id SERIAL PRIMARY KEY,
    price_list_id INT NOT NULL REFERENCES price_lists(id) ON DELETE CASCADE,
    locale VARCHAR(10) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM',
    UNIQUE(price_list_id, locale)
);

-- 4. Create price_list_routes table
CREATE TABLE IF NOT EXISTS price_list_routes (
    id SERIAL PRIMARY KEY,
    price_list_id INT NOT NULL UNIQUE REFERENCES price_lists(id) ON DELETE CASCADE,
    route_id BIGINT NOT NULL,
    waiting_fee NUMERIC(15, 2) DEFAULT 0,
    max_waiting_min INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM'
);

CREATE INDEX IF NOT EXISTS idx_price_list_routes_route ON price_list_routes(route_id);

-- 5. Create price_list_daily_rates table
CREATE TABLE IF NOT EXISTS price_list_daily_rates (
    id SERIAL PRIMARY KEY,
    price_list_id INT NOT NULL UNIQUE REFERENCES price_lists(id) ON DELETE CASCADE,
    min_days INT NOT NULL DEFAULT 1,
    max_days INT,
    km_included_per_day NUMERIC(10, 2),
    overage_price_per_km NUMERIC(15, 2),
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM'
);

-- 6. Clean up old incorrect price data from intercity_route_prices
DELETE FROM intercity_route_prices;
