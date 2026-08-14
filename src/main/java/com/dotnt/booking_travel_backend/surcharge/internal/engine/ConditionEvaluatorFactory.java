package com.dotnt.booking_travel_backend.surcharge.internal.engine;

import com.dotnt.booking_travel_backend.surcharge.api.PricingContext;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConditionEvaluatorFactory {

    private final List<ConditionEvaluator> evaluators;

    private static final ConditionEvaluator ALWAYS_TRUE_EVALUATOR = new ConditionEvaluator() {
        @Override
        public boolean supports(String conditionType) {
            return true;
        }

        @Override
        public boolean evaluate(JsonNode condition, PricingContext context) {
            return true;
        }
    };

    public ConditionEvaluator resolve(JsonNode condition) {
        if (condition == null || condition.isNull() || !condition.has("type")) {
            return ALWAYS_TRUE_EVALUATOR;
        }

        String type = condition.get("type").asText();
        return evaluators.stream()
                .filter(e -> e.supports(type))
                .findFirst()
                .orElseGet(() -> {
                    log.warn("No ConditionEvaluator found for type '{}'. Defaulting to ALWAYS_TRUE.", type);
                    return ALWAYS_TRUE_EVALUATOR;
                });
    }
}
