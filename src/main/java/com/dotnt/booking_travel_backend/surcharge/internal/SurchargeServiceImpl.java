package com.dotnt.booking_travel_backend.surcharge.internal;

import com.dotnt.booking_travel_backend.surcharge.api.*;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.ModuleSurchargeMapping;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.SurchargeRule;
import com.dotnt.booking_travel_backend.surcharge.internal.engine.ConditionEvaluator;
import com.dotnt.booking_travel_backend.surcharge.internal.engine.ConditionEvaluatorFactory;
import com.dotnt.booking_travel_backend.surcharge.internal.engine.SurchargeCalculator;
import com.dotnt.booking_travel_backend.surcharge.internal.registry.ModuleRegistry;
import com.dotnt.booking_travel_backend.surcharge.internal.repository.ModuleSurchargeMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurchargeServiceImpl implements SurchargeService {

    private final ModuleSurchargeMappingRepository mappingRepository;
    private final ConditionEvaluatorFactory evaluatorFactory;
    private final SurchargeCalculator calculator;
    private final ModuleRegistry moduleRegistry;

    @Override
    public SurchargeResult calculate(String moduleCode, PricingContext context) {
        if (context == null) {
            return SurchargeResult.empty(BigDecimal.ZERO);
        }

        BigDecimal basePrice = context.getBasePrice() != null ? context.getBasePrice() : BigDecimal.ZERO;
        if (moduleCode == null || moduleCode.isBlank()) {
            return SurchargeResult.empty(basePrice);
        }

        String normalizedCode = moduleCode.toUpperCase();
        if (!moduleRegistry.exists(normalizedCode)) {
            log.warn("Surcharge calculation requested for unregistered module: {}", normalizedCode);
        }

        List<ModuleSurchargeMapping> mappings = mappingRepository.findActiveMappingsWithRule(normalizedCode);
        if (mappings.isEmpty()) {
            return SurchargeResult.empty(basePrice);
        }

        List<SurchargeLineItem> lineItems = new ArrayList<>();
        BigDecimal runningTotal = basePrice;
        BigDecimal totalSurcharge = BigDecimal.ZERO;
        LocalDate serviceDate = context.getServiceDate();

        for (ModuleSurchargeMapping mapping : mappings) {
            SurchargeRule rule = mapping.getSurchargeRule();
            if (rule == null || !rule.isActive()) {
                continue;
            }

            // Check effective dates
            if (serviceDate != null) {
                if (rule.getEffectiveFrom() != null && serviceDate.isBefore(rule.getEffectiveFrom())) {
                    continue;
                }
                if (rule.getEffectiveTo() != null && serviceDate.isAfter(rule.getEffectiveTo())) {
                    continue;
                }
            }

            ConditionEvaluator evaluator = evaluatorFactory.resolve(rule.getCondition());
            if (evaluator.evaluate(rule.getCondition(), context)) {
                SurchargeLineItem item = calculator.apply(rule, basePrice, runningTotal);
                if (item != null && item.getAmount() != null) {
                    runningTotal = runningTotal.add(item.getAmount());
                    totalSurcharge = totalSurcharge.add(item.getAmount());
                    lineItems.add(item);
                }
            }
        }

        return SurchargeResult.builder()
                .lineItems(lineItems)
                .totalSurcharge(totalSurcharge)
                .finalAmount(runningTotal)
                .build();
    }

    @Override
    public List<ModuleDescriptor> getRegisteredModules() {
        return moduleRegistry.getAll();
    }
}
