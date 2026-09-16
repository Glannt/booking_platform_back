package com.dotnt.booking_travel_backend.promotion.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionConditionDto {
    private Long id;
    private String conditionType;
    private String operator;
    private String conditionValue;
}
