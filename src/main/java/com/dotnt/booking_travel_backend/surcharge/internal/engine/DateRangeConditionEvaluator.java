package com.dotnt.booking_travel_backend.surcharge.internal.engine;

import com.dotnt.booking_travel_backend.surcharge.api.PricingContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;

@Component
public class DateRangeConditionEvaluator implements ConditionEvaluator {

    private static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    @Override
    public boolean supports(String conditionType) {
        return "date_range".equalsIgnoreCase(conditionType) || "seasonal".equalsIgnoreCase(conditionType);
    }

    @Override
    public boolean evaluate(JsonNode condition, PricingContext context) {
        if (context == null || context.getServiceTime() == null) {
            return false;
        }

        if (!condition.has("from") || !condition.has("to")) {
            return true;
        }

        String fromStr = condition.get("from").asText();
        String toStr = condition.get("to").asText();
        LocalDate actualDate = context.getServiceTime().toLocalDate();

        try {
            // Check if full ISO date (yyyy-MM-dd)
            if (fromStr.length() == 10 && toStr.length() == 10) {
                LocalDate fromDate = LocalDate.parse(fromStr);
                LocalDate toDate = LocalDate.parse(toStr);
                return !actualDate.isBefore(fromDate) && !actualDate.isAfter(toDate);
            }

            // Month-Day format (MM-dd)
            MonthDay fromMd = MonthDay.parse(fromStr, MONTH_DAY_FORMATTER);
            MonthDay toMd = MonthDay.parse(toStr, MONTH_DAY_FORMATTER);
            MonthDay actualMd = MonthDay.from(actualDate);

            if (fromMd.equals(toMd)) {
                return actualMd.equals(fromMd);
            }

            if (fromMd.isBefore(toMd)) {
                // Same year range, e.g. 01-20 to 02-15
                return !actualMd.isBefore(fromMd) && !actualMd.isAfter(toMd);
            } else {
                // Cross year-end range, e.g. 12-20 to 01-10
                return !actualMd.isBefore(fromMd) || !actualMd.isAfter(toMd);
            }
        } catch (Exception e) {
            return false;
        }
    }
}
