package com.dotnt.booking_travel_backend.discount.api.dtos;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountResultDto {
    private boolean valid;
    private String discountCode;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String message;
}
