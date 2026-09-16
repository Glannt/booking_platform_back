-- =====================================================================
-- V011__Create_Shared_Route_Module.sql
--
-- Implements tasks/shared_route_implement.md:
--   * shared_routes (parent route container)
--   * shared_offices (physical pickup/dropoff offices)
--   * shared_route_directions (OUTBOUND, RETURN directions)
--   * shared_route_direction_origin_offices (M-N join table)
--   * shared_route_direction_destination_offices (M-N join table)
--   * shared_route_schedules (fixed departure times per direction)
--   * shared_route_bookings (customer ticket booking records)
-- =====================================================================

-- 1. Create shared_routes table
CREATE TABLE IF NOT EXISTS shared_routes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    vehicle_name VARCHAR(100) DEFAULT 'Dcar Limousine',
    active BOOLEAN DEFAULT TRUE NOT NULL,
    slug VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM'
);

-- 2. Create shared_offices table
CREATE TABLE IF NOT EXISTS shared_offices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    latitude NUMERIC(10, 7),
    longitude NUMERIC(10, 7),
    active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM'
);

CREATE INDEX IF NOT EXISTS idx_shared_offices_city ON shared_offices(city, active);

-- 3. Create shared_route_directions table
CREATE TABLE IF NOT EXISTS shared_route_directions (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL REFERENCES shared_routes(id) ON DELETE CASCADE,
    direction_type VARCHAR(20) NOT NULL, -- 'OUTBOUND', 'RETURN'
    origin_city VARCHAR(100) NOT NULL,
    destination_city VARCHAR(100) NOT NULL,
    origin_lat NUMERIC(10, 7),
    origin_lng NUMERIC(10, 7),
    destination_lat NUMERIC(10, 7),
    destination_lng NUMERIC(10, 7),
    price NUMERIC(15, 2) NOT NULL DEFAULT 250000.00,
    distance_km NUMERIC(8, 1),
    estimated_hours NUMERIC(5, 1),
    active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM'
);

CREATE INDEX IF NOT EXISTS idx_shared_directions_route ON shared_route_directions(route_id, direction_type, active);

-- 4. Create M-N table for origin offices
CREATE TABLE IF NOT EXISTS shared_route_direction_origin_offices (
    direction_id BIGINT NOT NULL REFERENCES shared_route_directions(id) ON DELETE CASCADE,
    office_id BIGINT NOT NULL REFERENCES shared_offices(id) ON DELETE CASCADE,
    PRIMARY KEY (direction_id, office_id)
);

-- 5. Create M-N table for destination offices
CREATE TABLE IF NOT EXISTS shared_route_direction_destination_offices (
    direction_id BIGINT NOT NULL REFERENCES shared_route_directions(id) ON DELETE CASCADE,
    office_id BIGINT NOT NULL REFERENCES shared_offices(id) ON DELETE CASCADE,
    PRIMARY KEY (direction_id, office_id)
);

-- 6. Create shared_route_schedules table
CREATE TABLE IF NOT EXISTS shared_route_schedules (
    id BIGSERIAL PRIMARY KEY,
    direction_id BIGINT NOT NULL REFERENCES shared_route_directions(id) ON DELETE CASCADE,
    departure_time TIME NOT NULL,
    days_of_week VARCHAR(20),
    active BOOLEAN DEFAULT TRUE NOT NULL,
    note VARCHAR(255),
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM'
);

CREATE INDEX IF NOT EXISTS idx_shared_schedules_direction ON shared_route_schedules(direction_id, departure_time, active);

-- 7. Create shared_route_bookings table
CREATE TABLE IF NOT EXISTS shared_route_bookings (
    id BIGSERIAL PRIMARY KEY,
    direction_id BIGINT NOT NULL REFERENCES shared_route_directions(id),
    schedule_id BIGINT NOT NULL REFERENCES shared_route_schedules(id),
    trip_date DATE NOT NULL,
    pickup_type VARCHAR(20) NOT NULL, -- 'DOOR', 'OFFICE'
    pickup_office_id BIGINT REFERENCES shared_offices(id),
    pickup_address VARCHAR(500),
    dropoff_type VARCHAR(20) NOT NULL, -- 'DOOR', 'OFFICE'
    dropoff_office_id BIGINT REFERENCES shared_offices(id),
    dropoff_address VARCHAR(500),
    passenger_count INT NOT NULL DEFAULT 1,
    total_price NUMERIC(15, 2) NOT NULL,
    booking_group_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    customer_name VARCHAR(255),
    customer_phone VARCHAR(50),
    customer_email VARCHAR(255),
    note TEXT,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM'
);

CREATE INDEX IF NOT EXISTS idx_shared_bookings_group ON shared_route_bookings(booking_group_id);
CREATE INDEX IF NOT EXISTS idx_shared_bookings_date ON shared_route_bookings(trip_date, status);

-- 8. Seed Sample Physical Offices
INSERT INTO shared_offices (id, name, address, city, latitude, longitude, active) VALUES
(1, 'VP Nha Trang - Trần Phú', '120 Trần Phú, Lộc Thọ, Nha Trang, Khánh Hòa', 'Nha Trang', 12.238791, 109.196749, TRUE),
(2, 'VP Nha Trang - Bến xe Phía Nam', 'Km 6 Đường 23/10, Vĩnh Trung, Nha Trang', 'Nha Trang', 12.250100, 109.155000, TRUE),
(3, 'VP Đà Lạt - Phan Bội Châu', '22 Phan Bội Châu, Phường 1, TP. Đà Lạt, Lâm Đồng', 'Đà Lạt', 11.942440, 108.438530, TRUE),
(4, 'VP Đà Lạt - Bến xe Liên Tỉnh', '01 Tô Hiến Thành, Phường 3, TP. Đà Lạt, Lâm Đồng', 'Đà Lạt', 11.928810, 108.444980, TRUE)
ON CONFLICT (id) DO NOTHING;

SELECT setval('shared_offices_id_seq', (SELECT COALESCE(MAX(id), 1) FROM shared_offices));

-- 9. Seed Sample Shared Route (Nha Trang ⇌ Đà Lạt)
INSERT INTO shared_routes (id, name, vehicle_name, slug, active) VALUES
(1, 'Nha Trang - Đà Lạt', 'Dcar Limousine 9 chỗ', 'nha-trang-da-lat-shared', TRUE)
ON CONFLICT (id) DO NOTHING;

SELECT setval('shared_routes_id_seq', (SELECT COALESCE(MAX(id), 1) FROM shared_routes));

-- 10. Seed 2 Directions (OUTBOUND: Nha Trang -> Đà Lạt, RETURN: Đà Lạt -> Nha Trang)
INSERT INTO shared_route_directions (id, route_id, direction_type, origin_city, destination_city, price, distance_km, estimated_hours, active) VALUES
(1, 1, 'OUTBOUND', 'Nha Trang', 'Đà Lạt', 250000.00, 135.0, 3.5, TRUE),
(2, 1, 'RETURN', 'Đà Lạt', 'Nha Trang', 250000.00, 135.0, 3.5, TRUE)
ON CONFLICT (id) DO NOTHING;

SELECT setval('shared_route_directions_id_seq', (SELECT COALESCE(MAX(id), 1) FROM shared_route_directions));

-- Assign Offices to Outbound (Pick at Nha Trang VP 1, 2; Drop at Da Lat VP 3, 4)
INSERT INTO shared_route_direction_origin_offices (direction_id, office_id) VALUES (1, 1), (1, 2) ON CONFLICT DO NOTHING;
INSERT INTO shared_route_direction_destination_offices (direction_id, office_id) VALUES (1, 3), (1, 4) ON CONFLICT DO NOTHING;

-- Assign Offices to Return (Pick at Da Lat VP 3, 4; Drop at Nha Trang VP 1, 2)
INSERT INTO shared_route_direction_origin_offices (direction_id, office_id) VALUES (2, 3), (2, 4) ON CONFLICT DO NOTHING;
INSERT INTO shared_route_direction_destination_offices (direction_id, office_id) VALUES (2, 1), (2, 2) ON CONFLICT DO NOTHING;

-- 11. Seed Departure Schedules for both directions (07:00, 09:00, 11:00, 13:00, 15:00, 17:00)
INSERT INTO shared_route_schedules (direction_id, departure_time, active, note) VALUES
(1, '07:00:00', TRUE, 'Chuyến sáng sớm'),
(1, '09:00:00', TRUE, 'Chuyến sáng tiêu chuẩn'),
(1, '11:00:00', TRUE, 'Chuyến trưa'),
(1, '13:00:00', TRUE, 'Chuyến đầu giờ chiều'),
(1, '15:00:00', TRUE, 'Chuyến xế chiều'),
(1, '17:00:00', TRUE, 'Chuyến chiều muộn'),
(2, '07:00:00', TRUE, 'Chuyến sáng sớm'),
(2, '09:00:00', TRUE, 'Chuyến sáng tiêu chuẩn'),
(2, '11:00:00', TRUE, 'Chuyến trưa'),
(2, '13:00:00', TRUE, 'Chuyến đầu giờ chiều'),
(2, '15:00:00', TRUE, 'Chuyến xế chiều'),
(2, '17:00:00', TRUE, 'Chuyến chiều muộn')
ON CONFLICT DO NOTHING;
