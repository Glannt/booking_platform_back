package com.dotnt.booking_travel_backend.promotion.api.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionDto {
    private Long id;
    private String name;
    private String description;
    private String code;
    private String issuanceType; // PUBLIC, PRIVATE_TARGETED, AUTO_REWARD
    private String discountType; // PERCENT, FIXED_AMOUNT
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private Integer minDays;
    private BigDecimal minOrderAmount;
    private String customerSegment;
    private boolean stackable;
    private Integer priority;
    private Integer maxUsageTotal;
    private Integer maxUsagePerUser;
    private Integer usageCount;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Boolean active;
    private String status;

    @Builder.Default
    private List<PromotionScopeDto> scopes = new ArrayList<>();

    @Builder.Default
    private List<PromotionConditionDto> conditions = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
