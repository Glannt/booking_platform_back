package com.dotnt.booking_travel_backend.surcharge.internal.engine;

import com.dotnt.booking_travel_backend.surcharge.api.PricingContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class RouteConditionEvaluator implements ConditionEvaluator {

    @Override
    public boolean supports(String conditionType) {
        return "route".equalsIgnoreCase(conditionType);
    }

    @Override
    public boolean evaluate(JsonNode condition, PricingContext context) {
        if (context == null || context.getRouteId() == null) {
            return false;
        }

        if (condition.has("ids") && condition.get("ids").isArray()) {
            Set<Long> ids = new HashSet<>();
            for (JsonNode node : condition.get("ids")) {
                ids.add(node.asLong());
            }
            return ids.contains(context.getRouteId());
        }

        if (condition.has("id")) {
            return condition.get("id").asLong() == context.getRouteId();
        }

        return true;
    }
}
