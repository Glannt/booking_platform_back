# Quick Reference: MasterEntity & Utils

## 📌 One-Page Quick Reference

### Creating a New Entity

```java
package com.dotnt.booking_travel_backend.your_module;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "your_table")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DiscriminatorValue("YOUR_ENTITY")  // ← Unique discriminator value
public class YourEntity extends MasterEntity {
    
    @Column(name = "field_name", nullable = false)
    private String fieldName;
    
    @Column(name = "amount")
    private Double amount;
    
    // Inherited from MasterEntity:
    // - id (auto-generated)
    // - createdAt (auto-populated)
    // - updatedAt (auto-populated)
    // - deletedAt (for soft delete)
    // - createdBy (currently "SYSTEM")
}
```

---

### Using Utils

#### StringUtils
```java
StringUtils.isEmpty(str)              // Check null or empty
StringUtils.isBlank(str)              // Check null or blank
StringUtils.capitalize("hello")       // → "Hello"
StringUtils.equalsIgnoreCase(a, b)    // Case-insensitive compare
```

#### ValidationUtils
```java
ValidationUtils.isValidEmail("user@example.com")
ValidationUtils.isValidPhone("+1234567890")
ValidationUtils.validateNotEmpty(value, "fieldName")  // Throws if empty
ValidationUtils.validatePositive(id, "id")            // Throws if not > 0
ValidationUtils.validateLength(str, 5, 50, "password")
```

#### DateTimeUtils
```java
DateTimeUtils.now()                      // Current LocalDateTime
DateTimeUtils.formatDateTime(dateTime)   // Format: yyyy-MM-dd HH:mm:ss
DateTimeUtils.parseDateTime("2026-06-27 10:30:00")
DateTimeUtils.isPast(dateTime)
DateTimeUtils.isFuture(dateTime)
DateTimeUtils.isToday(dateTime)
```

#### PageableUtils
```java
Pageable p1 = PageableUtils.getPageable(0);           // Page 0, size 20
Pageable p2 = PageableUtils.getPageable(0, 50);       // Page 0, size 50
Pageable p3 = PageableUtils.getPageableWithSort(0, 20, "name", "asc");
```

#### ConversionUtils
```java
Long id = ConversionUtils.toLong("123")
Integer count = ConversionUtils.toInteger("50")
Double price = ConversionUtils.toDouble("19.99")
Boolean flag = ConversionUtils.toBoolean("true")
// All support default values: toLong("abc", 0L) → 0L
```

---

### Common Patterns

#### Service with Validation & Utils

```java
@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository repository;
    
    public MenuDTO createMenu(CreateMenuRequest req) {
        // Validate input
        ValidationUtils.validateNotEmpty(req.getName(), "name");
        ValidationUtils.validateLength(req.getName(), 3, 100, "name");
        ValidationUtils.validatePositive(req.getPrice(), "price");
        
        // Create entity
        Menu menu = Menu.builder()
            .name(StringUtils.capitalize(req.getName().trim()))
            .description(req.getDescription())
            .price(req.getPrice())
            .isActive(true)
            .build();
        
        return mapToDTO(repository.save(menu));
    }
    
    public Page<MenuDTO> getAllMenus(int page, int size) {
        // Get safe pageable
        Pageable pageable = PageableUtils.getPageable(page, size)
        return repository.findAll(pageable).map(this::mapToDTO);
    }
}
```

#### Soft Delete

```java
public void deleteMenu(Long id) {
    Menu menu = repository.findById(id).orElseThrow();
    menu.setDeletedAt(DateTimeUtils.now());
    repository.save(menu);
}

// Query (exclude deleted)
Page<Menu> getActiveMenus(Pageable p) {
    return repository.findByDeletedAtIsNull(p);
}
```

---

### Database Schema (SINGLE_TABLE)

```sql
-- One table for all entities
CREATE TABLE master_entity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type VARCHAR(31) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'SYSTEM',
    -- Menu fields below
    name VARCHAR(255),
    description VARCHAR(500),
    price DECIMAL(10,2),
    is_active BOOLEAN
);

-- discriminator values: MENU, USER, BOOKING, etc.
```

---

### Lombok in MasterEntity

```java
@Data              // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // No-arg constructor
@AllArgsConstructor // All-arg constructor
@Builder           // Builder pattern: Menu.builder().name("x").build()
@DiscriminatorValue("MENU")  // For SINGLE_TABLE strategy
public class Menu extends MasterEntity {
    // Fields
}
```

---

### Imports Quick Lookup

```java
// MasterEntity
import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;

// Utils
import com.dotnt.booking_travel_backend.shared.utils.StringUtils;
import com.dotnt.booking_travel_backend.shared.utils.ValidationUtils;
import com.dotnt.booking_travel_backend.shared.utils.DateTimeUtils;
import com.dotnt.booking_travel_backend.shared.utils.PageableUtils;
import com.dotnt.booking_travel_backend.shared.utils.ConversionUtils;

// Lombok
import lombok.*;

// JPA
import jakarta.persistence.*;

// Spring
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
```

---

### Checklist Before Creating Entity

- [ ] Extend MasterEntity
- [ ] Add @Entity & @Table
- [ ] Add @Data, @NoArgsConstructor, @AllArgsConstructor, @Builder from Lombok
- [ ] Add @DiscriminatorValue("UNIQUE_NAME")
- [ ] Define custom fields with @Column
- [ ] Create Repository extending JpaRepository
- [ ] Create DTO/Request/Response classes
- [ ] Add service layer with validation
- [ ] Update database migration if needed

---

### Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Discriminator value not working | Add @DiscriminatorValue on each entity |
| Timestamps not auto-populating | Ensure @EnableJpaAuditing configured & @EntityListeners applied |
| Validation not working | Import from correct utils & validate before save |
| Soft delete not working | Query with `WHERE deleted_at IS NULL` |
| Lombok not generating code | Check build.gradle has annotation processor |

---

### Documentation

- **Full README**: `src/main/java/com/dotnt/booking_travel_backend/shared/README.md`
- **Implementation Details**: `IMPLEMENTATION_SUMMARY.md`
- **Q&A**: `QA_IMPLEMENTATION.md`
- **Example Usage**: `MenuUtilitiesExample.java`

---

### Future Updates

1. After **User Module**: Integrate AuditorAware for createdBy
2. Add soft-delete query filters using Hibernate
3. Add more utils as needed
4. Move hardcoded "SYSTEM" to Spring Security context

---

**Created**: June 27, 2026  
**Version**: 1.0

