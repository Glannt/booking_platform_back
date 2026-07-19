-- V004: Add can_rent column to vehicles table

ALTER TABLE vehicles
ADD COLUMN can_rent BOOLEAN NOT NULL DEFAULT TRUE;
