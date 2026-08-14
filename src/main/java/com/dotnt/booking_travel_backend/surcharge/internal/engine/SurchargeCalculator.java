package com.dotnt.booking_travel_backend.surcharge.internal.engine;

import com.dotnt.booking_travel_backend.surcharge.api.SurchargeLineItem;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.CalculationBase;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.CalculationType;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.SurchargeRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class SurchargeCalculator {

    public SurchargeLineItem apply(SurchargeRule rule, BigDecimal basePrice, BigDecimal runningTotal) {
        BigDecimal base = (rule.getCalculationBase() == CalculationBase.BASE_PRICE)
                ? (basePrice != null ? basePrice : BigDecimal.ZERO)
                : (runningTotal != null ? runningTotal : BigDecimal.ZERO);

        BigDecimal amount;
        if (rule.getCalculationType() == CalculationType.PERCENT) {
            BigDecimal rate = rule.getValue() != null ? rule.getValue() : BigDecimal.ZERO;
            amount = base.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            amount = rule.getValue() != null ? rule.getValue() : BigDecimal.ZERO;
        }

        return SurchargeLineItem.builder()
                .ruleId(rule.getId())
                .code(rule.getCode())
                .name(rule.getName())
                .amount(amount)
                .calculationType(rule.getCalculationType() != null ? rule.getCalculationType().name() : "FIXED")
                .rate(rule.getValue())
                .build();
    }
}
