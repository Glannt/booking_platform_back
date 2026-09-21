ALTER TABLE intercity_seasonal_surcharges ADD COLUMN IF NOT EXISTS currency VARCHAR(3);
ALTER TABLE intercity_seasonal_surcharges ADD COLUMN IF NOT EXISTS priority INTEGER;