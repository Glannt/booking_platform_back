# Legacy Migrations — REFERENCE ONLY

Các file `V001__…` → `V013__…` ở đây là migration **cũ**, chỉ giữ lại để tham khảo lịch sử:

- Nằm ở `db/legacy-migrations/` (repo root), **không** nằm trong classpath của ứng dụng.
- Bằng chứng chúng **chưa từng được áp dụng** lên production: `V013` yêu cầu `RENAME rentals → self_drive_rentals`, nhưng bảng `rentals` (5 rows) vẫn tồn tại trong `booking_travel_prod`.
- **Flyway đã được gỡ khỏi project.** Schema hiện được Hibernate quản lý qua `spring.jpa.hibernate.ddl-auto` (`JPA_DDL_AUTO`, mặc định `update`) — phù hợp với DB mới/rỗng.

**Không sao chép các file này vào `src/main/resources`.** Nếu cần đổi schema, sửa entity (Hibernate tự cập nhật) hoặc chạy script SQL thủ công.
