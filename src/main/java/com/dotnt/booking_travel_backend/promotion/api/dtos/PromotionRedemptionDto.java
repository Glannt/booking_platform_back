package com.dotnt.booking_travel_backend.promotion.api.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionRedemptionDto {
    private Long id;
    private Long promotionId;
    private String promotionCode;
    private Long grantId;
    private Long customerId;
    private Long orderId;
    private BigDecimal discountAmount;
    private String status;
    private LocalDateTime redeemedAt;
    private LocalDateTime reversedAt;
}
