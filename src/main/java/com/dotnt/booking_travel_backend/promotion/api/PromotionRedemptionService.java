package com.dotnt.booking_travel_backend.promotion.api;

import com.dotnt.booking_travel_backend.promotion.api.dtos.PromotionRedemptionDto;

import java.math.BigDecimal;
import java.util.List;

public interface PromotionRedemptionService {

    PromotionRedemptionDto redeemPromotion(Long promotionId, Long grantId, Long customerId, Long orderId, BigDecimal discountAmount);

    void reverseRedemption(Long orderId);

    List<PromotionRedemptionDto> getRedemptionsByOrderId(Long orderId);

    List<PromotionRedemptionDto> getRedemptionsByPromotionId(Long promotionId);
}
