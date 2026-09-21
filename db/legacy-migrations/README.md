# Legacy Migrations — REFERENCE ONLY

Các file `V001__…` → `V013__…` ở đây là migration **cũ và KHÔNG được Flyway chạy**:

- Chúng nằm ở `db/legacy-migrations/` (repo root), **không** nằm trong `src/main/resources/db/migration` nên không bao giờ vào classpath của ứng dụng.
- Bằng chứng chúng **chưa từng được áp dụng** lên production: `V013` yêu cầu `RENAME rentals → self_drive_rentals`, nhưng bảng `rentals` (5 rows) vẫn tồn tại trong `booking_travel_prod`.
- Schema hiện tại được quản lý **hoàn toàn bởi Flyway** bắt đầu từ `src/main/resources/db/migration/V1__baseline.sql` (sinh từ entity model, không phải từ các file này).

**Không sao chép các file này vào thư mục Flyway.** Mọi thay đổi schema mới phải là `V<n>__description.sql` trong `src/main/resources/db/migration/`.