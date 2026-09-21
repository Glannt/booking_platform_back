ALTER TABLE intercity_routes ADD COLUMN IF NOT EXISTS origin_name VARCHAR(255);
ALTER TABLE intercity_routes ADD COLUMN IF NOT EXISTS destination_name VARCHAR(255);