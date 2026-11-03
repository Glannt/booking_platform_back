# Runbook — Migration DB bằng Flyway & Deploy Production

> ⚠️ **KHÔNG CÒN ÁP DỤNG (11/2026):** Flyway và toàn bộ `src/main/resources/db/migration` đã được gỡ khỏi project. DB mới/rỗng nên schema do Hibernate quản lý qua `spring.jpa.hibernate.ddl-auto` (`JPA_DDL_AUTO=update`). Tài liệu này chỉ giữ lại để tham khảo lịch sử.

> Trạng thái: **Đã test local thành công** (xem §2). Áp dụng cho `minhkhoatravel` production.
> Repo: `booking_travel_backend` (branch `dev`, merge → `main` để trigger CI).

---

## 1. Vì sao cần runbook này

- `booking_travel_prod` có dữ liệu thật (6 bookings, 127 locations, 2 users, 14 sections, …) → **phải giữ dữ liệu**.
- Schema prod bị **drift** so với entity (`ddl-auto=update` không thêm được cột `NOT NULL` — `tour_category.code` → backend crash lúc start).
- Các migration cũ `db/legacy-migrations/V001–V013` **chưa từng được áp** ⇒ không thể dùng làm baseline.
- Giải pháp: **Flyway** quản lý schema; Hibernate chỉ `validate`. Schema chuẩn = `V1__baseline.sql` sinh từ entity.

## 2. Kết quả đã kiểm chứng LOCAL (đã chạy thật)

| Test | Kịch bản | Kết quả |
|---|---|---|
| 1 | DB trống `flyway_test` + `ddl-auto=validate` + Flyway ON | ✅ Flyway áp `V1__baseline.sql` (100 bảng, 19s), Hibernate validate pass, app `Started` |
| 2 | DB đã có schema (`flyway_baseline`, không có `flyway_schema_history`) — giống prod | ✅ Flyway tạo history với dòng **BASELINE version 1** (KHÔNG chạy lại SQL), app `Started` |

Lệnh xác nhận trên prod sau khi deploy:

```sql
SELECT installed_rank, version, description, type, success, execution_time
FROM flyway_schema_history ORDER BY installed_rank;
-- Test 1: version=1, description=baseline, type=SQL,  success=t
-- Test 2: version=1, description=<< Flyway Baseline >>, type=BASELINE, success=t
```

## 3. Kiến trúc mới (đã commit trong repo)

- `build.gradle`: `spring-boot-starter-flyway` + `org.flywaydb:flyway-database-postgresql` (bắt buộc — Flyway 12 core KHÔNG hỗ trợ Postgres nếu thiếu module này; lỗi `Unsupported Database: PostgreSQL`).
- `src/main/resources/application.yaml`:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:update}   # dev=update, prod=validate
  flyway:
    enabled: ${FLYWAY_ENABLED:true}
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 1
    validate-on-migrate: true
```

- `src/main/resources/db/migration/V1__baseline.sql` — schema chuẩn (pg_dump của DB do Hibernate tạo từ entity, đã bỏ dòng `\restrict`/`\unrestrict` của pg_dump 18).
- `src/test/resources/application.yaml`: `spring.flyway.enabled: false` (test dùng H2 + create-drop).
- `db/migration/` cũ → đổi tên `db/legacy-migrations/` + README (reference only).
- `DatabasePreMigrationConfig` vẫn còn (no-op sau khi schema chuẩn) — xem cảnh báo §8.

## 4. Triển khai lên Production LẦN ĐẦU (một lần duy nhất)

### Bước 0 — Backup prod (BẮT BUỘC trước mọi thứ)

```bash
cd /opt/projects/minhkhoatravel && set -a && . .env && set +a
docker exec minhkhoatravel-postgres pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc -f /tmp/prod.dump
docker cp minhkhoatravel-postgres:/tmp/prod.dump /opt/backups/prod-$(date +%F-%H%M).dump
ls -lh /opt/backups/
```

### Bước 1 — Copy dump về máy local (test migration trên clone)

```bash
# Từ máy local (Windows PowerShell)
scp root@<VPS_IP>:/opt/backups/prod-<date>.dump C:\Users\<you>\Downloads\prod.dump
```

### Bước 2 — Restore clone local

```bash
docker exec booking_travel_postgres psql -U myuser -d postgres -c "DROP DATABASE IF EXISTS prod_clone;"
docker exec booking_travel_postgres psql -U myuser -d postgres -c "CREATE DATABASE prod_clone;"
# copy dump vào container rồi restore
docker cp prod.dump booking_travel_postgres:/tmp/prod.dump
docker exec -i booking_travel_postgres pg_restore -U myuser -d prod_clone --clean --if-exists /tmp/prod.dump
```

### Bước 3 — Khởi tạo DB chuẩn (baseline) nếu chưa có

`V1__baseline.sql` đã nằm sẵn trong repo. Cách regenerate khi entity đổi **toàn bộ** (hiếm):

```bash
docker exec booking_travel_postgres psql -U myuser -d postgres -c "DROP DATABASE IF EXISTS base_tmp;"
docker exec booking_travel_postgres psql -U myuser -d postgres -c "CREATE DATABASE base_tmp;"
# chạy backend với JPA_DDL_AUTO=create, FLYWAY_ENABLED=false, URL=base_tmp
# rồi dump:
docker exec booking_travel_postgres pg_dump -U myuser -d base_tmp --schema-only --no-owner --no-privileges -f /tmp/base.sql
# BỎ 2 dòng meta-command pg_dump 18:  \restrict ...  và  \unrestrict ...
# (dùng Notepad++ / sed, KHÔNG dùng PowerShell Set-Content -Encoding UTF8 kẻo thêm BOM)
docker cp booking_travel_postgres:/tmp/base.sql src/main/resources/db/migration/V1__baseline.sql
```

> Kể từ đây mọi thay đổi schema = file `V2__xxx.sql`, `V3__xxx.sql` … trong
> `src/main/resources/db/migration/`. **KHÔNG** bao giờ quay lại `ddl-auto=update` ở prod.

### Bước 4 — Sinh `sync.sql` (diff toàn bộ prod → baseline)

Dùng `migra` (không cần cài Python nhờ Docker):

```bash
docker run --rm djrobstep/migra \
  "postgresql://myuser:secret@host.docker.internal:5432/prod_clone" \
  "postgresql://myuser:secret@host.docker.internal:5432/flyway_baseline" > sync.sql
```

> `flyway_baseline` = DB đã áp `V1__baseline.sql` (hoặc `base_tmp`). `host.docker.internal` trỏ về máy local.

### Bước 5 — Rà soát `sync.sql` (QUAN TRỌNG: cột NOT NULL mới)

`migra` xuất thẳng `ADD COLUMN ... NOT NULL` — câu đó **fail** nếu bảng có dữ liệu.
Luật: cột NOT NULL mới phải tách 3 bước:

```sql
-- ❌ migra có thể xuất thế này → PHẢI sửa:
-- ALTER TABLE tour_category ADD COLUMN code varchar(50) NOT NULL;

-- ✅ Sửa thành:
ALTER TABLE tour_category ADD COLUMN IF NOT EXISTS code varchar(50);
UPDATE tour_category SET code = 'CAT_' || id WHERE code IS NULL;
ALTER TABLE tour_category ALTER COLUMN code SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_tour_category_code ON tour_category(code);
```

- Bảng legacy (entity đã bỏ: `rentals`, `rental_types`, `rental_prices`, `service_*`, `province_*`, …): **KHÔNG drop** trong `sync.sql` — để sang lần sau khi chắc chắn (xem §8).
- Bảng hiện có dữ liệu nhưng thiếu cột mới: thêm nullable → backfill → set NOT NULL (như trên).

### Bước 6 — Test migration trên clone (bắt buộc trước khi đụng prod)

```bash
docker exec -i booking_travel_postgres psql -U myuser -d prod_clone < sync.sql
# Chạy backend trỏ vào prod_clone với validate:
#   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/prod_clone?options=-c%20timezone=UTC
#   JPA_DDL_AUTO=validate  FLYWAY_ENABLED=true
# Kỳ vọng: Start sạch, KHÔNG có Schema-validation error, KHÔNG có PSQLException.
# Kiểm tra dữ liệu không mất:
SELECT count(*) FROM bookings; SELECT count(*) FROM locations; SELECT id,email FROM users;
```

Chỉ khi log sạch + dữ liệu đủ → mới sang Bước 7.

### Bước 7 — Áp lên PROD

```bash
docker cp sync.sql minhkhoatravel-postgres:/tmp/sync.sql
docker exec -i minhkhoatravel-postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" < sync.sql
# kiểm tra nhanh
docker exec minhkhoatravel-postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT count(*) FROM bookings;"
```

### Bước 8 — Deploy backend (lần này sẽ tạo baseline + validate)

Cập nhật `deploy.yml` trên VPS (phần `backend.environment`) — **bắt buộc để prod không bao giờ tự sửa schema**:

```yaml
      # Database Connection
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB}?options=-c%20timezone=UTC
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
      # Schema management: Flyway là nguồn chân lý, Hibernate chỉ validate
      JPA_DDL_AUTO: validate
      FLYWAY_ENABLED: "true"
```

Deploy:

```bash
cd /opt/projects/minhkhoatravel
docker compose up -d --build backend   # lần đầu thủ công
docker compose logs -f backend | grep -E "Flyway|baseline|Started|ERROR"
```

Kỳ vọng log: `Successfully baselined schema with version: 1` → `Schema "public" is up to date` → `Started BookingTravelBackendApplication`.

### Bước 9 — Deploy frontend

```bash
cd /opt/projects/minhkhoatravel
./scripts/deploy-bluegreen.sh frontend
```

## 5. Fix `deploy-bluegreen.sh` (frontend health-check sai → rollback vô ích)

**Nguyên nhân:** Next đã `Ready` nhưng script probe `wget http://localhost:3000` bị `Connection refused`
(nghi vấn busybox wget resolve `localhost` ra IPv6 `::1` trong khi Next bind IPv4 `0.0.0.0`).
Chẩn đoán trên VPS:

```bash
docker exec minhkhoatravel-frontend-blue wget --spider -q http://localhost:3000 ; echo "localhost exit=$?"
docker exec minhkhoatravel-frontend-blue wget --spider -q http://127.0.0.1:3000 ; echo "127 exit=$?"
```

Sửa trong script (3 chỗ):

1. Health URL dùng IP tường minh:
   - backend: `HEALTH_URL="http://127.0.0.1:8080/actuator/health"`
   - frontend: `HEALTH_URL="http://127.0.0.1:3000/api/health"` (route mới đã thêm: `src/app/api/health/route.ts`)
2. Container frontend **KHÔNG** truyền `--env-file "$PROJECT_DIR/.env"` (file chứa biến backend, dễ ghi đè `PORT`/`HOSTNAME`). Truyền tường minh:

```bash
-e NODE_ENV=production -e PORT=3000 -e HOSTNAME=0.0.0.0 -e NEXT_PUBLIC_BACKEND_API="https://${API_DOMAIN}"
```

3. Tăng `--memory 384m → 512m` cho frontend nếu thấy OOM trong `docker stats`.

## 6. GitHub Actions (build & push image)

Workflow hiện tại: `.github/workflows/deploy.yml` — **chỉ build image và push Docker Hub**, không SSH, không gọi Render hook.

- Trigger: `push: branches: [main]` + `workflow_dispatch`.
- Tag đẩy lên: `latest` và `<git-sha>` (giữ tag sha để rollback).
- Render chạy service kiểu **Existing Image** → sau khi workflow chạy xong phải vào Render bấm **Manual Deploy** (image-backed service không tự redeploy khi có tag mới).

```yaml
      - name: Build & push image
        uses: docker/build-push-action@v6
        with:
          context: .
          platforms: linux/amd64
          push: true
          tags: |
            ${{ env.IMAGE_NAME }}:latest
            ${{ env.IMAGE_NAME }}:${{ github.sha }}
          cache-from: type=gha
          cache-to: type=gha,mode=max
```

- Thêm `concurrency` (không để 2 build cùng lúc).

## 7. Checklist setup TAY (không CI hoá được)

- [ ] `/opt/projects/minhkhoatravel/.env` tồn tại đủ biến theo `deploy.yml`.
- [ ] `deploy.yml` trên VPS đã thêm `JPA_DDL_AUTO: validate` + `FLYWAY_ENABLED: "true"`.
- [ ] `scripts/deploy-bluegreen.sh` đã sửa theo §5 (health `127.0.0.1` + `/api/health`).
- [ ] `/opt/backups` + cron backup DB định kỳ.
- [ ] GitHub secrets repo backend: `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN` (không còn `VPS_*`).
- [ ] Render: service **Existing Image** trỏ `docker.io/<DOCKERHUB_USERNAME>/booking-platform-backend:latest`, env theo `.env.production`.
- [ ] DB: đã chạy `sync.sql` trên prod (§4 Bước 7) — **chỉ sau khi test sạch ở Bước 6**.

## 8. Cảnh báo quan trọng

1. **`DatabasePreMigrationConfig` (hack cũ) còn trong code.** Nó DROP bảng `role_permissions` nếu thiếu cột `role_id`, chạy TRƯỚC Flyway. Sau `sync.sql`, cột đã có → vô hại. **Khuyến nghị xoá class này sau 1–2 tuần ổn định** (Flyway đã thay vai trò).
2. **Blue-green hiện không phải "switch" nguyên tử**: 2 màu dùng chung Traefik router name + Host rule → Traefik load-balance như 2 replica. Trong lúc deploy, **code cũ + mới cùng chạy**.
3. **Hệ quả cho migration:** mọi `V<n>` **bắt buộc backward-compatible** (chỉ thêm cột/bảng/index; KHÔNG drop/rename/đổi type ngay). Drop bảng legacy chỉ làm ở lần sau, khi chắc chắn code cũ đã tắt.
4. **Không dùng PowerShell `Set-Content -Encoding UTF8` để sửa file SQL** (thêm BOM). Dùng save-as UTF-8 no BOM.
5. Migration chạy **trong transaction** — nếu 1 lệnh fail, Flyway rollback cả migration và container start fail → blue-green tự rollback. Đó là lý do phải test trên clone trước.

## 9. Rollback

- Frontend/backend cũ: `./scripts/deploy-bluegreen.sh <svc> <previous-sha>` (tag SHA đã đẩy lên DockerHub).
- DB: không cần rollback schema (migration backward-compatible); nếu cần, restore backup:

```bash
docker cp /opt/backups/prod-<date>.dump minhkhoatravel-postgres:/tmp/restore.dump
docker exec -i minhkhoatravel-postgres pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" --clean --if-exists /tmp/restore.dump
```

> Lưu ý: `pg_restore --clean` sẽ xoá bảng `flyway_schema_history` do Flyway tạo (nó không thuộc dump cũ) — chỉ dùng khi thực sự cần.