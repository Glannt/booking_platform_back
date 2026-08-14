package com.dotnt.booking_travel_backend.surcharge.internal.engine;

import com.dotnt.booking_travel_backend.surcharge.api.PricingContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class TimeRangeConditionEvaluator implements ConditionEvaluator {

    @Override
    public boolean supports(String conditionType) {
        return "time_range".equalsIgnoreCase(conditionType);
    }

    @Override
    public boolean evaluate(JsonNode condition, PricingContext context) {
        if (context == null || context.getServiceTime() == null) {
            return false;
        }

        if (!condition.has("from") || !condition.has("to")) {
            return true;
        }

        try {
            LocalTime from = LocalTime.parse(condition.get("from").asText());
            LocalTime to = LocalTime.parse(condition.get("to").asText());
            LocalTime actual = context.getServiceTime().toLocalTime();

            if (from.equals(to)) {
                return true;
            }

            if (from.isBefore(to)) {
                // Normal daytime interval, e.g. 08:00 to 17:00
                return !actual.isBefore(from) && !actual.isAfter(to);
            } else {
                // Overnight interval, e.g. 22:00 to 05:00
                return !actual.isBefore(from) || !actual.isAfter(to);
            }
        } catch (Exception e) {
            return false;
        }
    }
}
