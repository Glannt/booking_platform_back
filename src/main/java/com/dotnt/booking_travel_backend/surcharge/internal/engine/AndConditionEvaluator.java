package com.dotnt.booking_travel_backend.surcharge.internal.engine;

import com.dotnt.booking_travel_backend.surcharge.api.PricingContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class AndConditionEvaluator implements ConditionEvaluator {

    private final ConditionEvaluatorFactory factory;

    public AndConditionEvaluator(@Lazy ConditionEvaluatorFactory factory) {
        this.factory = factory;
    }

    @Override
    public boolean supports(String conditionType) {
        return "and".equalsIgnoreCase(conditionType);
    }

    @Override
    public boolean evaluate(JsonNode condition, PricingContext context) {
        if (!condition.has("conditions") || !condition.get("conditions").isArray()) {
            return true;
        }

        for (JsonNode sub : condition.get("conditions")) {
            ConditionEvaluator evaluator = factory.resolve(sub);
            if (!evaluator.evaluate(sub, context)) {
                return false;
            }
        }
        return true;
    }
}
