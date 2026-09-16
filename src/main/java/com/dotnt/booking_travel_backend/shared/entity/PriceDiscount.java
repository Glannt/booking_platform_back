package com.dotnt.booking_travel_backend.shared.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_discounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceDiscount extends MasterEntity {

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "price_list_id")
    private Long priceListId;

    @Column(name = "price_type_id")
    private Long priceTypeId;

    @Column(name = "scope_type", length = 50)
    private String scopeType; // GLOBAL, INTERCITY_ROUTE, SHARED_ROUTE, SELF_DRIVE, TOUR, AIRPORT_TRANSFER

    @Column(name = "scope_ref_id", length = 100)
    private String scopeRefId;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "discount_type", length = 20, nullable = false)
    private String discountType; // PERCENT | FIXED_AMOUNT

    @Column(name = "discount_value", precision = 14, scale = 4, nullable = false)
    private BigDecimal discountValue;

    @Column(name = "min_days")
    private Integer minDays;

    @Column(name = "min_order_amount", precision = 14, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "customer_segment", length = 50)
    private String customerSegment;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Column(name = "max_usage")
    private Integer maxUsage;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean active = true;
}
