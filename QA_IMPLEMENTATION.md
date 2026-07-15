# Q&A: MasterEntity & Utils Implementation

## Questions & Answers

### 1. **Why SINGLE_TABLE Inheritance Strategy?**

**Q: Tại sao chọn SINGLE_TABLE thay vì TABLE_PER_CLASS?**

**A:**
- SINGLE_TABLE strategy sử dụng một bảng duy nhất cho tất cả entities
- Sử dụng discriminator column (`entity_type`) để phân biệt loại entity
- **Advantages:**
  - Một query đơn giản để lấy dữ liệu từ tất cả entities
  - Tốc độ join nhanh hơn
  - Schema database đơn giản và dễ maintain
  - Phù hợp với modulith architecture
- **Trade-offs:**
  - Bảng có thể chứa nhiều nullable columns
  - Cần quản lý discriminator column

### 2. **Softcoded createdBy?**

**Q: Tại sao hardcode createdBy là "SYSTEM"?**

**A:**
- Hiện tại chưa có user module
- Sau khi user module implementation xong, sẽ integrate Spring Security context
- Implementation later:
  ```java
  @Component
  public class UserAuditorAware implements AuditorAware<String> {
      @Override
      public Optional<String> getCurrentAuditor() {
          Authentication auth = SecurityContextHolder.getContext().getAuthentication();
          if (auth != null && auth.isAuthenticated()) {
              return Optional.of(auth.getName());
          }
          return Optional.of("SYSTEM");
      }
  }
  ```

### 3. **Soft Delete Implementation?**

**Q: Làm sao implement soft delete?**

**A:**
- Field `deletedAt` đã thêm vào MasterEntity
- Khi soft delete: set `deletedAt = now()`
- Khi query: cần add `WHERE deleted_at IS NULL` clause
- Options:
  1. Custom repository methods
  2. Hibernate `@SQLDelete` annotation
  3. Hibernate Envers for audit trail

### 4. **Lombok Annotations?**

**Q: Tại sao dùng @Data thay vì @Getter/@Setter?**

**A:**
- `@Data` = `@Getter` + `@Setter` + `@ToString` + `@EqualsAndHashCode` + `@RequiredArgsConstructor`
- Giảm boilerplate code
- Lombok đã configured trong build.gradle
- Note: Khi extend MasterEntity, nên sử dụng `@Data` để đồng bộ hóa

### 5. **Package Structure?**

**Q: Tại sao tách shared module riêng?**

**A:**
- Modulith best practice
- Shared code reusable across all modules
- Clear separation of concerns
- `entity/` - Base entities
- `utils/` - Utility classes
- Easy to maintain and test

### 6. **JpaAuditingConfig?**

**Q: Làm sao enable JPA auditing?**

**A:**
- `@Configuration` + `@EnableJpaAuditing` trên configuration class
- `@EntityListeners(AuditingEntityListener.class)` trên entity
- `@CreatedDate` + `@LastModifiedDate` trên timestamp fields
- Spring data tự động populate timestamps

### 7. **Entity Package Info?**

**Q: Tại sao cần package-info.java?**

**A:**
- Documentation tool
- Enable package-level annotations
- Javadoc for packages
- Best practice for modulith

### 8. **Utils Reusability?**

**Q: Làm sao dùng utils từ các modules khác?**

**A:**
```java
import com.dotnt.booking_travel_backend.shared.utils.StringUtils;
import com.dotnt.booking_travel_backend.shared.utils.ValidationUtils;

// Use directly
StringUtils.isNotBlank(input);
ValidationUtils.isValidEmail(email);
```

### 9. **MasterEntity Abstract?**

**Q: Tại sao MasterEntity không abstract?**

**A:**
- `@MappedSuperclass` không thể abstract (không hợp lệ)
- Nhưng nó không phải entity riêng biệt
- Chỉ dùng để kế thừa
- Không thể instantiate trực tiếp (không có `@Entity`)

### 10. **DiscriminatorValue Format?**

**Q: Tên DiscriminatorValue nên là gì?**

**A:**
- Recommend: UPPERCASE entity name
- Examples: "MENU", "USER", "BOOKING", "MEDIA", "PAGE", "SERVICE"
- Hoặc: CamelCase "Menu", "User", "Booking"
- Important: Phải unique cho mỗi entity class

---

## Recommendations for Future Implementation

### For Other Modules

1. **Create entity extending MasterEntity:**
```java
@Entity
@Table(name = "your_entity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DiscriminatorValue("YOUR_ENTITY")
public class YourEntity extends MasterEntity {
    // Custom fields...
}
```

2. **Use Utils wherever applicable:**
```java
// Validation
ValidationUtils.validateNotEmpty(username, "username");
ValidationUtils.validateLength(password, 8, 50, "password");

// String operations
if (StringUtils.isNotBlank(email)) { ... }

// DateTime operations
var createdToday = DateTimeUtils.isToday(entity.getCreatedAt());

// Pagination
Pageable page = PageableUtils.getPageable(0, 20);

// Type conversion
Long userId = ConversionUtils.toLong(userIdString);
```

### Performance Considerations

- SINGLE_TABLE with discriminator is optimized by most databases
- Create index on discriminator column + created_at for better query performance
- For soft delete queries, create index on deleted_at column

### Testing

- Test MasterEntity inheritance
- Test auditing timestamps auto-population
- Test utility functions edge cases
- Test soft delete logic

---

## Implementation Checklist

- [x] Create MasterEntity with @MappedSuperclass
- [x] Add SINGLE_TABLE inheritance strategy
- [x] Add discriminator column configuration
- [x] Create JpaAuditingConfig
- [x] Create StringUtils
- [x] Create ValidationUtils
- [x] Create DateTimeUtils
- [x] Create PageableUtils
- [x] Create ConversionUtils
- [x] Update Menu.java as example
- [x] Create README.md
- [x] Create example usage
- [x] Create SQL migration reference
- [ ] Test MasterEntity inheritance (next step)
- [ ] Test auditing functionality (next step)
- [ ] Implement user module (future)
- [ ] Integrate user context (future)

