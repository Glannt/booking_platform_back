package com.dotnt.booking_travel_backend.promotion.api.dtos;

import com.dotnt.booking_travel_backend.promotion.internal.scope.CartItem;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidatePromotionRequest {
    private String code;
    private Long customerId;
    private String scopeType;
    private String scopeRefId;
    private BigDecimal orderAmount;
    private Integer days;
    private List<CartItem> cartItems;
    private String customerSegment;
}
