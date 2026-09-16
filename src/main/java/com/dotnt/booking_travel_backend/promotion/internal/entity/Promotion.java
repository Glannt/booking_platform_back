package com.dotnt.booking_travel_backend.promotion.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import com.dotnt.booking_travel_backend.shared.enums.DiscountType;
import com.dotnt.booking_travel_backend.shared.enums.IssuanceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion extends MasterEntity {

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "code", length = 50, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "issuance_type", length = 20, nullable = false)
    @Builder.Default
    private IssuanceType issuanceType = IssuanceType.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", length = 20, nullable = false)
    @Builder.Default
    private DiscountType discountType = DiscountType.PERCENT;

    @Column(name = "discount_value", precision = 14, scale = 4, nullable = false)
    private BigDecimal discountValue;

    @Column(name = "max_discount_amount", precision = 14, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_days")
    private Integer minDays;

    @Column(name = "min_order_amount", precision = 14, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "customer_segment", length = 50)
    private String customerSegment;

    @Builder.Default
    @Column(name = "stackable", nullable = false)
    private boolean stackable = false;

    @Builder.Default
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @Column(name = "max_usage_total")
    private Integer maxUsageTotal;

    @Builder.Default
    @Column(name = "max_usage_per_user", nullable = false)
    private Integer maxUsagePerUser = 1;

    @Builder.Default
    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PromotionScope> scopes = new ArrayList<>();

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PromotionCondition> conditions = new ArrayList<>();

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PromotionGrant> grants = new ArrayList<>();
}
