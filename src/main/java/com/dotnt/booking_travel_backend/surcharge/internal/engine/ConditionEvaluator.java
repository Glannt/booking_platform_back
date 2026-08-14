package com.dotnt.booking_travel_backend.surcharge.internal.engine;

import com.dotnt.booking_travel_backend.surcharge.api.PricingContext;
import com.fasterxml.jackson.databind.JsonNode;

public interface ConditionEvaluator {
    boolean supports(String conditionType);
    boolean evaluate(JsonNode condition, PricingContext context);
}
