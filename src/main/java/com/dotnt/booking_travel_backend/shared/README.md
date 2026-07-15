# Shared Module Documentation

The Shared module contains common entities, configurations, and utilities for use across all modules in the modulith.

## Structure

```
shared/
├── entity/
│   ├── MasterEntity.java       # Base entity class for all entities
│   └── package-info.java
├── utils/
│   ├── StringUtils.java        # String manipulation utilities
│   ├── ValidationUtils.java    # Input validation utilities
│   ├── DateTimeUtils.java      # Date/time formatting utilities
│   ├── PageableUtils.java      # Pagination helper utilities
│   ├── ConversionUtils.java    # Type conversion utilities
│   └── package-info.java
├── JpaAuditingConfig.java      # JPA Auditing configuration
├── package-info.java
└── README.md (this file)
```

## MasterEntity

`MasterEntity` is a base class for all entities in the modulith, providing common fields:

- `id` - Primary key (auto-generated)
- `createdAt` - Timestamp when the entity was created (auto-populated)
- `updatedAt` - Timestamp when the entity was last modified (auto-populated)
- `deletedAt` - Soft delete timestamp (nullable)
- `createdBy` - User who created the entity (currently hardcoded as "SYSTEM")

### Key Features

- **SINGLE_TABLE Inheritance Strategy** - All subclasses share the same database table with a discriminator column
- **Automatic Auditing** - Timestamps are automatically populated via Spring Data JPA auditing
- **Soft Delete Support** - Use `deletedAt` field for soft deletion
- **JpaAuditingConfig** - Configuration class that enables auditing via `@EnableJpaAuditing`

### How to Use MasterEntity

1. Extend `MasterEntity` in your entity class:

```java
@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DiscriminatorValue("USER")
public class User extends MasterEntity {
    
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    
    @Column(name = "email", nullable = false)
    private String email;
    
    // Other fields...
}
```

2. Important annotations:
   - `@Entity` - Marks the class as a JPA entity
   - `@DiscriminatorValue("YOUR_ENTITY_NAME")` - Sets the discriminator value for SINGLE_TABLE inheritance
   - `@Data` - Lombok annotation that generates getters, setters, equals, hashCode, toString
   - `@Builder` - Lombok annotation for builder pattern

3. MasterEntity automatically provides:
   - Primary key management
   - CreatedAt/UpdatedAt timestamps
   - Auditing support

## Utilities

### StringUtils

String manipulation and validation:
```java
StringUtils.isEmpty("text")        // Check if string is empty
StringUtils.isBlank("  ")          // Check if string is blank
StringUtils.capitalize("hello")    // Capitalize first letter
```

### ValidationUtils

Input validation:
```java
ValidationUtils.isValidEmail("user@example.com")
ValidationUtils.isValidPhone("+1234567890")
ValidationUtils.validateNotEmpty(username, "username")
ValidationUtils.validateNotNull(user, "user")
ValidationUtils.validateLength(email, 5, 255, "email")
```

### DateTimeUtils

Date and time operations:
```java
DateTimeUtils.formatDateTime(LocalDateTime.now())
DateTimeUtils.parseDateTime("2026-06-27 10:30:00")
DateTimeUtils.now()                // Current datetime
DateTimeUtils.isPast(dateTime)
DateTimeUtils.isFuture(dateTime)
DateTimeUtils.isToday(dateTime)
```

### PageableUtils

Pagination helper:
```java
Pageable pageable = PageableUtils.getPageable(0, 20);  // Page 0, size 20
Pageable pageable = PageableUtils.getPageableWithSort(0, 20, "name", "asc");
```

### ConversionUtils

Type conversion:
```java
Long id = ConversionUtils.toLong("123");
Integer count = ConversionUtils.toInteger("50");
Boolean flag = ConversionUtils.toBoolean("true");
```

## JPA Auditing Configuration

The `JpaAuditingConfig` class automatically enables JPA auditing:

```java
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // Automatically enables @CreatedDate and @LastModifiedDate annotations
}
```

To use auditing in your entities:
1. Extend `MasterEntity` (already configured)
2. Add `@EntityListeners(AuditingEntityListener.class)` (already included in MasterEntity)
3. The `createdAt` and `updatedAt` fields are automatically populated

## Future Enhancements

### UserContext Integration (After User Module Implementation)

Currently, `createdBy` is hardcoded to "SYSTEM". After user module implementation:

1. Implement `AuditorAware<String>` interface:
```java
@Component
public class UserAuditorAware implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return Optional.of(authentication.getName());
        }
        return Optional.of("SYSTEM");
    }
}
```

2. Update `JpaAuditingConfig`:
```java
@Bean
public AuditorAware<String> auditorAware() {
    return new UserAuditorAware();
}
```

### Soft Delete Filtering

To automatically filter out soft-deleted entities:

1. Use Hibernate `@SQLDelete` annotation
2. Or implement custom repository methods
3. Consider using Hibernate Envers for audit history

## Notes

- All modules should extend `MasterEntity` for their entities
- Use utilities from shared.utils throughout the application
- Follow the naming conventions for discriminator values in subclasses
- Always use `@Data` with `@EqualsAndHashCode(callSuper = true)` if overriding equality in subclasses

