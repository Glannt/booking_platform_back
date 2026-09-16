package com.dotnt.booking_travel_backend.promotion.api.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionGrantDto {
    private Long id;
    private Long promotionId;
    private String promotionName;
    private String promotionCode;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderAmount;
    private String description;
    private Long customerId;
    private String customerEmail;
    private String customerName;
    private String uniqueCode;
    private String grantedReason;
    private LocalDateTime grantedAt;
    private LocalDateTime expiresAt;
    private boolean used;
    private LocalDateTime usedAt;
    private Long usedOrderId;
}
