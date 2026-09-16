package com.dotnt.booking_travel_backend.promotion.api;

import com.dotnt.booking_travel_backend.promotion.api.dtos.PromotionResultDto;
import com.dotnt.booking_travel_backend.promotion.api.dtos.ValidatePromotionRequest;

public interface PromotionValidationService {
    PromotionResultDto validateAndApplyPromotion(ValidatePromotionRequest request);
}
