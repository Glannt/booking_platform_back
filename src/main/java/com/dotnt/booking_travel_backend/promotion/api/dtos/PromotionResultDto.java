package com.dotnt.booking_travel_backend.promotion.api.dtos;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionResultDto {
    private boolean valid;
    private Long promotionId;
    private Long grantId;
    private String discountCode;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String message;
}
