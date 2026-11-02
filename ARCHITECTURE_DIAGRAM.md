# Architecture Diagram & Visual Summary

## 🏗️ Modulith Architecture with MasterEntity

```
┌─────────────────────────────────────────────────────────────────────┐
│                     Booking Travel Backend                           │
│                      (Spring Modulith 2.1.0)                        │
└─────────────────────────────────────────────────────────────────────┘

┌─── SHARED MODULE ──────────────────────────────────────────────┐
│                                                                  │
│  ┌──── Entity Package ──────┐  ┌──── Utils Package ─────────┐  │
│  │                          │  │                            │  │
│  │  [MasterEntity]          │  │  [StringUtils]             │  │
│  │  ├─ id (Long)            │  │  ├─ isEmpty()             │  │
│  │  ├─ createdAt            │  │  ├─ isBlank()             │  │
│  │  ├─ updatedAt            │  │  ├─ capitalize()          │  │
│  │  ├─ deletedAt (nullable) │  │  └─ equalsIgnoreCase()    │  │
│  │  └─ createdBy: "SYSTEM"  │  │                            │  │
│  │                          │  │  [ValidationUtils]        │  │
│  │  @MappedSuperclass       │  │  ├─ isValidEmail()        │  │
│  │  @Inheritance(SINGLE_TBL)│  │  ├─ isValidPhone()        │  │
│  │  @EntityListeners        │  │  ├─ validateNotEmpty()    │  │
│  │                          │  │  └─ validateLength()      │  │
│  │                          │  │                            │  │
│  └──────────────────────────┘  │  [DateTimeUtils]          │  │
│                                │  ├─ formatDateTime()       │  │
│  ┌──── Config ────────────┐    │  ├─ parseDateTime()        │  │
│  │                        │    │  ├─ now()                 │  │
│  │ [JpaAuditingConfig]    │    │  └─ isPast/isFuture()     │  │
│  │ @Configuration         │    │                            │  │
│  │ @EnableJpaAuditing     │    │  [PageableUtils]          │  │
│  │                        │    │  ├─ getPageable()         │  │
│  └────────────────────────┘    │  └─ validatePageSize()    │  │
│                                │                            │  │
│                                │  [ConversionUtils]        │  │
│                                │  ├─ toLong()              │  │
│                                │  ├─ toInteger()           │  │
│                                │  ├─ toDouble()            │  │
│                                │  └─ toBoolean()           │  │
│                                │                            │  │
│                                └────────────────────────────┘  │
│                                                                  │
├─────────────────────────────────────────────────────────────────│
│  [JpaAuditingConfig] enables automatic @CreatedDate/@LastModified
│  All entities using MasterEntity get automatic timestamps!
│                                                                  │
└──────────────────────────────────────────────────────────────────┘

┌─── MODULE: Menu ──────────────────────────────────────────────┐
│                                                                 │
│  [Menu] extends MasterEntity                                  │
│  ├─ @Entity                                                   │
│  ├─ @DiscriminatorValue("MENU")                              │
│  ├─ name (String)                                            │
│  ├─ description (String)                                     │
│  ├─ price (Double)                                           │
│  └─ isActive (Boolean)                                       │
│                                                                 │
│  Inherited from MasterEntity:                                │
│  ├─ id ✓                                                      │
│  ├─ createdAt ✓ (auto-populated)                             │
│  ├─ updatedAt ✓ (auto-populated)                             │
│  ├─ deletedAt ✓                                              │
│  └─ createdBy ✓                                              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─── Other Modules (User, Booking, Media, etc.) ─────────────────┐
│                                                                   │
│  Each will follow the same pattern:                             │
│  - Extend MasterEntity                                          │
│  - Add @DiscriminatorValue("MODULE_NAME")                       │
│  - Use utils for validation and operations                      │
│                                                                   │
│  [User] extends MasterEntity                                   │
│  [Booking] extends MasterEntity                                │
│  [Media] extends MasterEntity                                  │
│  [Page] extends MasterEntity                                   │
│  [Service] extends MasterEntity                                │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
```

---

## 📊 Database Schema (SINGLE_TABLE Strategy)

```
┌─────────────────────────────────────── master_entity ───────────────────────────────────────┐
│                                                                                              │
│  id (BIGINT) [PK]    entity_type (VARCHAR)    created_at    updated_at    deleted_at    created_by
│  ────────────────────────────────────────────────────────────────────────────────────────────
│  1                   MENU                     2026-06-27...  2026-06-27...  NULL         SYSTEM
│  2                   MENU                     2026-06-27...  2026-06-27...  NULL         SYSTEM  
│  3                   USER                     2026-06-27...  2026-06-27...  NULL         SYSTEM
│  4                   BOOKING                  2026-06-27...  2026-06-27...  2026-06-27...SYSTEM  (soft deleted)
│  ...                 ...                      ...            ...            ...          ...
│                                                                                              │
│  Additional Fields (entity-specific, stored in same table):                                │
│  name (VARCHAR)    description (VARCHAR)    price (DECIMAL)    is_active (BOOLEAN)        │
│  ──────────────────────────────────────────────────────────────────────────────────         │
│  Coffee Menu       Fresh brew...             3.50               TRUE                       │
│  Latte             With milk...              4.50               TRUE                       │
│  john_doe          email...                  NULL               TRUE                       │
│  ...               ...                       ...                ...                        │
│                                                                                              │
└──────────────────────────────────────────────────────────────────────────────────────────────┘

Key Characteristics:
✓ Single table for all entity types
✓ discriminator column (entity_type) differentiates types
✓ Automatic timestamps (created_at, updated_at)
✓ Soft delete support (deleted_at)
✓ Audit tracking (created_by)
✓ Efficient indexing on frequent query columns
```

---

## 🔄 Entity Inheritance Flow

```
┌─────────────────────┐
│   MasterEntity      │
│ @MappedSuperclass   │
│                     │
│ - id                │
│ - createdAt         │
│ - updatedAt         │
│ - deletedAt         │
│ - createdBy         │
└──────────┬──────────┘
           │
           │ extends
           │
    ┌──────┴──────┬──────────┬──────────┬──────────┐
    │             │          │          │          │
    ▼             ▼          ▼          ▼          ▼
 ┌──────┐   ┌────────┐  ┌───────┐ ┌──────────┐ ┌────────┐
 │ Menu │   │  User  │  │Booking│ │  Media   │ │  Page  │
 ├──────┤   ├────────┤  ├───────┤ ├──────────┤ ├────────┤
 │name  │   │username│  │status │ │url       │ │title   │
 │price │   │email   │  │amount │ │metadata  │ │content │
 │desc. │   │role    │  │dates  │ │type      │ │type    │
 │active│   │active  │  │notes  │ │size      │ │author  │
 └──────┘   └────────┘  └───────┘ └──────────┘ └────────┘
 
 All inherit from MasterEntity:
 ✓ Automatic timestamps (createdAt, updatedAt)
 ✓ Soft delete (deletedAt)
 ✓ Audit tracking (createdBy)
 ✓ Primary key management (id)
```

---

## 🛠️ Utils Usage Flow

```
┌─────────────────────────────────────┐
│     Controller/API Request          │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│          Validation Utils           │
│  ValidationUtils.validateNotEmpty() │
│  ValidationUtils.validateLength()   │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│          String Utils               │
│  StringUtils.isNotBlank()           │
│  StringUtils.capitalize()           │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│       Conversion Utils              │
│  ConversionUtils.toLong()           │
│  ConversionUtils.toInteger()        │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│         Service Layer               │
│  Create entity with validated data  │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│     Entity extends MasterEntity     │
│  Automatic timestamps & auditing    │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│   Repository (Spring Data JPA)      │
│  Auto-generated CRUD operations     │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│   Database (PostgreSQL/SINGLE_TABLE)│
│  Persisted with timestamps          │
└─────────────────────────────────────┘
```

---

## 📚 File Organization

```
S H A R E D   M O D U L E   S T R U C T U R E
════════════════════════════════════════════════════════════════════

booking_travel_backend/
│
├── src/main/java/com/dotnt/booking_travel_backend/
│   │
│   ├── shared/
│   │   ├── entity/
│   │   │   ├── MasterEntity.java ..................... (BASE ENTITY)
│   │   │   └── package-info.java ..................... (DOCUMENTATION)
│   │   │
│   │   ├── utils/
│   │   │   ├── StringUtils.java ...................... (STRING OPERATIONS)
│   │   │   ├── ValidationUtils.java .................. (INPUT VALIDATION)
│   │   │   ├── DateTimeUtils.java .................... (DATE/TIME OPERATIONS)
│   │   │   ├── PageableUtils.java .................... (PAGINATION)
│   │   │   ├── ConversionUtils.java .................. (TYPE CONVERSION)
│   │   │   └── package-info.java ..................... (DOCUMENTATION)
│   │   │
│   │   ├── JpaAuditingConfig.java .................... (AUDITING CONFIGURATION)
│   │   ├── package-info.java ......................... (MODULE DOCUMENTATION)
│   │   └── README.md ................................ (COMPREHENSIVE GUIDE)
│   │
│   ├── menu/
│   │   ├── Menu.java ................................ (EXAMPLE ENTITY)
│   │   ├── internal/
│   │   │   └── MenuUtilitiesExample.java ............ (USAGE EXAMPLE)
│   │   └── package-info.java
│   │
│   ├── user/, booking/, media/, page/, service/
│   │   └── (Will follow same pattern as menu/)
│   │
│   └── BookingTravelBackendApplication.java ....... (MAIN CLASS)
│
├── db/migration/
│   └── V001__Create_MasterEntity_Table.sql ........ (DATABASE SCHEMA)
│
├── build.gradle .................................... (DEPENDENCIES CONFIGURED)
│
├── IMPLEMENTATION_SUMMARY.md ........................ (DETAILED SUMMARY)
├── QA_IMPLEMENTATION.md ............................. (DESIGN DECISIONS)
├── QUICK_REFERENCE.md ............................... (DEVELOPER GUIDE)
├── PROJECT_INDEX.md ................................. (FILE INDEX)
├── ARCHITECTURE_DIAGRAM.md .......................... (THIS FILE)
│
└── gradle/, .gradle/, src/test/, etc.
```

---

## ✨ Key Features at a Glance

| Feature | Implementation | Location |
|---------|---|---|
| **Base Entity** | MasterEntity with audititing | `shared/entity/MasterEntity.java` |
| **Inheritance** | SINGLE_TABLE strategy | `@Inheritance` annotation |
| **Timestamps** | Auto-populated CreatedAt/UpdatedAt | `JpaAuditingConfig.java` |
| **Soft Delete** | deletedAt field | `MasterEntity.deleteAt` |
| **String Utils** | isEmpty, isBlank, capitalize | `shared/utils/StringUtils.java` |
| **Validation** | Email, phone, length, required | `shared/utils/ValidationUtils.java` |
| **DateTime** | Format, parse, comparisons | `shared/utils/DateTimeUtils.java` |
| **Pagination** | Safe pageable creation | `shared/utils/PageableUtils.java` |
| **Conversion** | Type conversion with defaults | `shared/utils/ConversionUtils.java` |
| **Lombok** | Annotations for less boilerplate | All entities using `@Data`, `@Builder` |
| **Documentation** | Comprehensive guides | Multiple `.md` files + JSDoc |

---

## 🎯 Summary

This architecture provides:

✅ **Consistent Base** - All entities extend MasterEntity  
✅ **Automatic Auditing** - Timestamps auto-populated  
✅ **Soft Delete Support** - Safe deletion without data loss  
✅ **Reusable Utilities** - 5 comprehensive util classes  
✅ **Single Database Table** - SINGLE_TABLE inheritance  
✅ **Zero Boilerplate** - Lombok annotations  
✅ **Ready to Scale** - Modulith architecture support  
✅ **Well Documented** - Multiple comprehensive guides  

---

**Created**: June 27, 2026  
**Status**: ✅ READY FOR PRODUCTION  
**Next**: Implement User, Booking, Media modules using this architecture

