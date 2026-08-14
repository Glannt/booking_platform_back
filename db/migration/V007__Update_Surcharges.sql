-- V007__Update_Surcharges.sql
-- Update night surcharge to apply to ALL airport transfers (not just specific routes)
-- Set airport_transfer_ids to NULL for "Phụ thu đêm khuya" so it applies to all airport transfers

UPDATE intercity_seasonal_surcharges
SET airport_transfer_ids = NULL
WHERE season_name = N'Phụ thu đêm khuya';