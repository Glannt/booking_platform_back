-- V2: Thêm cột `code` (NOT NULL, unique) cho tour_category.
-- Prod thiếu cột này so với V1 baseline; entity TourCategory khai báo @Column(name = "code", unique = true, nullable = false).
-- 3 bước an toàn: ADD nullable -> UPDATE dữ liệu -> SET NOT NULL (không fail trên bảng có dữ liệu).
ALTER TABLE tour_category ADD COLUMN IF NOT EXISTS code varchar(50);
UPDATE tour_category SET code = 'CAT_' || id WHERE code IS NULL;
ALTER TABLE tour_category ALTER COLUMN code SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_tour_category_code ON tour_category(code);