package com.dotnt.booking_travel_backend.promotion.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionScopeDto {
    private Long id;
    private String scopeType;
    private String scopeRefId;
}
