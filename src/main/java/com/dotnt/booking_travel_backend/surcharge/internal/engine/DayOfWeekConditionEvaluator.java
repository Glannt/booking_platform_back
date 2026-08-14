package com.dotnt.booking_travel_backend.surcharge.internal.engine;

import com.dotnt.booking_travel_backend.surcharge.api.PricingContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

@Component
public class DayOfWeekConditionEvaluator implements ConditionEvaluator {

    @Override
    public boolean supports(String conditionType) {
        return "day_of_week".equalsIgnoreCase(conditionType);
    }

    @Override
    public boolean evaluate(JsonNode condition, PricingContext context) {
        if (context == null || context.getServiceTime() == null) {
            return false;
        }

        if (!condition.has("days") || !condition.get("days").isArray()) {
            return true;
        }

        DayOfWeek actualDay = context.getServiceTime().getDayOfWeek();
        Set<String> allowedDays = new HashSet<>();
        for (JsonNode dayNode : condition.get("days")) {
            allowedDays.add(dayNode.asText().toUpperCase());
        }

        return allowedDays.contains(actualDay.name()) || allowedDays.contains(String.valueOf(actualDay.getValue()));
    }
}
