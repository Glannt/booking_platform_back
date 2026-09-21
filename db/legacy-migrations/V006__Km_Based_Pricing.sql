-- =====================================================================
-- V006__Km_Based_Pricing.sql
--
-- Converts pricing from fixed per-route x vehicle amounts to km-based:
--   * Airport transfers : base_km + base_amount + per_km_amount
--     (round trip is derived as 2 x one-way, so ROUND_TRIP rows are deleted)
--   * Intercity routes  : distance_km x per_km_amount
--
-- IMPORTANT: run this script BEFORE starting the new backend.
-- The schema is managed by Hibernate (ddl-auto: update); it adds new
-- columns/constraints automatically but NEVER drops columns/constraints,
-- so the old service_type constraint + columns must be removed here.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. airport_routes: add km + coordinate columns (Hibernate fills these
--    via the new admin route modal; safe defaults are NULL).
-- ---------------------------------------------------------------------
ALTER TABLE airport_routes ADD COLUMN IF NOT EXISTS distance_km NUMERIC(8, 1) NULL;
ALTER TABLE airport_routes ADD COLUMN IF NOT EXISTS origin_lat NUMERIC(10, 7) NULL;
ALTER TABLE airport_routes ADD COLUMN IF NOT EXISTS origin_lng NUMERIC(10, 7) NULL;
ALTER TABLE airport_routes ADD COLUMN IF NOT EXISTS destination_lat NUMERIC(10, 7) NULL;
ALTER TABLE airport_routes ADD COLUMN IF NOT EXISTS destination_lng NUMERIC(10, 7) NULL;

-- ---------------------------------------------------------------------
-- 2. airport_transfer_prices
-- ---------------------------------------------------------------------
-- 2a. Round trip is now derived (2 x one-way). Drop the ROUND_TRIP rows
--     so each (route, category) has exactly one row before the new
--     unique constraint is added by Hibernate at startup.
DELETE FROM airport_transfer_prices WHERE service_type = 'ROUND_TRIP';

-- 2b. Preserve existing one-way amounts as the new base amount.
ALTER TABLE airport_transfer_prices RENAME COLUMN amount TO base_amount;

-- 2c. New km-model columns. Existing rows get a sensible 35km / 10.000 VND
--     per extra km (admin can refine later via the price modal).
ALTER TABLE airport_transfer_prices ADD COLUMN IF NOT EXISTS base_km NUMERIC(10, 2) NOT NULL DEFAULT 35;
ALTER TABLE airport_transfer_prices ADD COLUMN IF NOT EXISTS per_km_amount NUMERIC(15, 2) NOT NULL DEFAULT 10000;

-- 2d. Drop the old unique constraint (airport_route_id, vehicle_category_id,
--     service_type) which references the soon-to-be-dropped column. The
--     constraint name is Hibernate-generated, so resolve it dynamically.
DO $$
DECLARE
  c TEXT;
BEGIN
  FOR c IN
    SELECT con.conname
    FROM pg_constraint con
    JOIN pg_attribute a ON a.attrelid = con.conrelid AND a.attnum = ANY(con.conkey)
    WHERE con.conrelid = 'airport_transfer_prices'::regclass
      AND con.contype = 'u'
      AND a.attname = 'service_type'
  LOOP
    EXECUTE format('ALTER TABLE airport_transfer_prices DROP CONSTRAINT %I', c);
  END LOOP;
END $$;

-- 2e. Remove the obsolete service_type column.
ALTER TABLE airport_transfer_prices DROP COLUMN IF EXISTS service_type;

-- ---------------------------------------------------------------------
-- 3. intercity_route_prices: replace one-way/round-trip amounts with
--    a per-km amount, backfilled from the old one-way price so existing
--    routes keep ~their previous pricing.
-- ---------------------------------------------------------------------
ALTER TABLE intercity_route_prices ADD COLUMN IF NOT EXISTS per_km_amount NUMERIC(15, 2) NOT NULL DEFAULT 10000;

UPDATE intercity_route_prices p
SET per_km_amount = GREATEST(1, ROUND(p.one_way_amount / NULLIF(r.distance_km, 0)))
FROM intercity_routes r
WHERE r.id = p.route_id
  AND r.distance_km IS NOT NULL
  AND r.distance_km > 0;

ALTER TABLE intercity_route_prices DROP COLUMN IF EXISTS one_way_amount;
ALTER TABLE intercity_route_prices DROP COLUMN IF EXISTS round_trip_amount;
