-- V3: Thêm cột đặt cọc cho `price_lists` — đặt cọc phụ thuộc từng loại giá.
-- Mỗi row `price_lists` đã là đúng MỘT price type (ONE_WAY / ROUND_TRIP_SAME_DAY / DAILY_RATE),
-- nên 3 cột này là đủ để mỗi loại giá mang tỉ lệ đặt cọc riêng. Dùng chung tên cột với
-- `tours`, `airport_routes`, `airport_transfer_prices` (đều đã map qua @Embedded DepositPolicy)
-- để không phải đổi logic ở các module khác.
--
-- Tất cả đều NULLABLE và có DEFAULT: một row chưa cấu hình đặt cọc phải giữ NULL, không được
-- điền sẵn 20% — nếu không thì "chưa cấu hình" và "cố ý đặt 20%" trông y hệt nhau trong DB và
-- không còn phân biệt được (xem tasks/plan.md mục "Airport precedence is unreachable").
ALTER TABLE price_lists ADD COLUMN IF NOT EXISTS deposit_type varchar(20);
ALTER TABLE price_lists ADD COLUMN IF NOT EXISTS deposit_rate numeric(5,2);
ALTER TABLE price_lists ADD COLUMN IF NOT EXISTS deposit_amount numeric(15,2);

COMMENT ON COLUMN price_lists.deposit_type IS 'PERCENT | FIXED | PER_PERSON. NULL = chưa cấu hình đặt cọc';
COMMENT ON COLUMN price_lists.deposit_rate IS 'Tỉ lệ % khi deposit_type = PERCENT';
COMMENT ON COLUMN price_lists.deposit_amount IS 'Số tiền cố định khi deposit_type = FIXED | PER_PERSON';