-- V003: Add has_departure_times to intercity_routes and create intercity_departure_times table

ALTER TABLE intercity_routes
ADD COLUMN has_departure_times BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE intercity_departure_times (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM',
    route_id BIGINT NOT NULL REFERENCES intercity_routes(id),
    departure_time VARCHAR(5) NOT NULL,
    order_index INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_departure_times_route_id ON intercity_departure_times(route_id);
CREATE INDEX idx_departure_times_active ON intercity_departure_times(active);
