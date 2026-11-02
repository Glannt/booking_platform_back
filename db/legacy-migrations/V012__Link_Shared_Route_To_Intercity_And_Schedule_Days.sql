-- =====================================================================
-- V012__Link_Shared_Route_To_Intercity_And_Schedule_Days.sql
--
-- 1. Add linked_intercity_route_id to shared_routes to connect with intercity_routes
-- 2. Create shared_route_schedule_days table for Many-to-Many schedule days of week
-- =====================================================================

-- 1. Add linked_intercity_route_id to shared_routes
ALTER TABLE shared_routes
ADD COLUMN IF NOT EXISTS linked_intercity_route_id BIGINT REFERENCES intercity_routes(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_shared_routes_linked_intercity
ON shared_routes(linked_intercity_route_id);

-- 2. Create shared_route_schedule_days table for Many-to-Many schedule days
CREATE TABLE IF NOT EXISTS shared_route_schedule_days (
    schedule_id BIGINT NOT NULL REFERENCES shared_route_schedules(id) ON DELETE CASCADE,
    day_of_week VARCHAR(20) NOT NULL, -- 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'
    PRIMARY KEY (schedule_id, day_of_week)
);

CREATE INDEX IF NOT EXISTS idx_schedule_days_day ON shared_route_schedule_days(day_of_week);

-- 3. Seed default schedule days for existing schedules (run daily)
INSERT INTO shared_route_schedule_days (schedule_id, day_of_week)
SELECT s.id, d.day_name
FROM shared_route_schedules s
CROSS JOIN (
    VALUES
        ('MONDAY'),
        ('TUESDAY'),
        ('WEDNESDAY'),
        ('THURSDAY'),
        ('FRIDAY'),
        ('SATURDAY'),
        ('SUNDAY')
) AS d(day_name)
ON CONFLICT (schedule_id, day_of_week) DO NOTHING;
