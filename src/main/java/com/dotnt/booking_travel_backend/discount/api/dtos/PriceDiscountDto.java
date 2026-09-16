package com.dotnt.booking_travel_backend.discount.api.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceDiscountDto {
    private Long id;
    private String name;
    private String description;
    private Long priceListId;
    private Long priceTypeId;
    private String scopeType;
    private String scopeRefId;
    private String code;
    private String discountType;
    private BigDecimal discountValue;
    private Integer minDays;
    private BigDecimal minOrderAmount;
    private String customerSegment;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Integer maxUsage;
    private Integer usageCount;
    private Boolean active;
    private String status;
}
