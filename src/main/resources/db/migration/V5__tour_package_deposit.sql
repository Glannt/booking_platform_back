-- V5: Chuyển đặt cọc từ `tours` sang `tour_packages`.
--
-- Deposit là tính chất của gói đang bán (mỗi package có thể có giá và chính sách cọc riêng),
-- nên 3 cột deposit_* chuyển từ `tours` sang `tour_packages`. Chỉ còn PERCENT | FIXED:
-- PER_PERSON là pricing type của package, không phải deposit type.
--
-- Backfill: copy deposit của tour sang MỌI package của tour đó. Tour chưa cấu hình
-- (deposit_type IS NULL) để nguyên NULL — không bịa ra default nào (chưa cấu hình = 0đ).
-- Idempotent: chỉ ghi đè khi package chưa có gì.
--
-- Cột cũ trên `tours` được GIỮ LẠI (entity không map nữa, ddl-auto update không drop;
-- các script report còn đọc chúng). Dọn ở lần migration schema riêng khi nào chắc chắn.
ALTER TABLE tour_packages ADD COLUMN IF NOT EXISTS deposit_type varchar(20);
ALTER TABLE tour_packages ADD COLUMN IF NOT EXISTS deposit_rate numeric(5,2);
ALTER TABLE tour_packages ADD COLUMN IF NOT EXISTS deposit_amount numeric(15,2);

UPDATE tour_packages tp
SET deposit_type   = t.deposit_type,
    deposit_rate   = t.deposit_rate,
    deposit_amount = t.deposit_amount
FROM tours t
WHERE t.id = tp.tour_id
  AND t.deposit_type IS NOT NULL
  AND tp.deposit_type IS NULL;

COMMENT ON COLUMN tour_packages.deposit_type IS 'PERCENT | FIXED. NULL = chưa cấu hình đặt cọc (không thu cọc)';
COMMENT ON COLUMN tour_packages.deposit_rate IS 'Tỉ lệ % khi deposit_type = PERCENT (tính trên tổng tiền vé sau discount)';
COMMENT ON COLUMN tour_packages.deposit_amount IS 'Số tiền cố định khi deposit_type = FIXED';
