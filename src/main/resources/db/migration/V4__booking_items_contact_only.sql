-- V4: Đánh dấu booking item là "đơn báo giá" (contact-only) ngay tại row đã đặt.
--
-- Khi admin bật `is_contact_only` trên gói tour, gói vẫn active và vẫn đặt được, nhưng không có
-- giá nên tổng đơn = 0đ. Ghi cờ ở đây thay vì thêm một giá trị BookingStatus, vì lý do cụ thể:
-- nếu admin sau này tắt cờ, những đơn 0đ cũ sẽ lại được tính như doanh thu thật. Cờ nằm trên
-- từng row nên phản ánh đúng trạng thái tại thời điểm đặt và không tự đổi theo cấu hình sau này.
--
-- Không backfill: chỉ booking mới được recompute (BookingServiceImpl.createBooking), lịch sử cũ
-- giữ nguyên. NULL = không phải đơn báo giá, để phân biệt với FALSE một cách tường minh.
ALTER TABLE booking_items ADD COLUMN IF NOT EXISTS is_contact_only boolean;

COMMENT ON COLUMN booking_items.is_contact_only IS 'TRUE = đơn báo giá (gói contact-only, không có giá). NULL/FALSE = đơn có giá thật';
