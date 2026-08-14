package com.dotnt.booking_travel_backend.surcharge.internal.engine;

import com.dotnt.booking_travel_backend.surcharge.api.PricingContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ZoneConditionEvaluator implements ConditionEvaluator {

    @Override
    public boolean supports(String conditionType) {
        return "zone".equalsIgnoreCase(conditionType);
    }

    @Override
    public boolean evaluate(JsonNode condition, PricingContext context) {
        if (context == null || context.getZoneCode() == null) {
            return false;
        }

        if (condition.has("in") && condition.get("in").isArray()) {
            Set<String> zones = new HashSet<>();
            for (JsonNode node : condition.get("in")) {
                zones.add(node.asText());
            }
            return zones.contains(context.getZoneCode());
        }

        if (condition.has("zone")) {
            return condition.get("zone").asText().equalsIgnoreCase(context.getZoneCode());
        }

        return true;
    }
}
