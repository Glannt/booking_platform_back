package com.dotnt.booking_travel_backend.surcharge.internal.config;

import com.dotnt.booking_travel_backend.surcharge.internal.domain.CalculationBase;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.CalculationType;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.ModuleSurchargeMapping;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.SurchargeRule;
import com.dotnt.booking_travel_backend.surcharge.internal.repository.ModuleSurchargeMappingRepository;
import com.dotnt.booking_travel_backend.surcharge.internal.repository.SurchargeRuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(30)
public class SurchargeDataInitializer implements CommandLineRunner {

    private final SurchargeRuleRepository ruleRepository;
    private final ModuleSurchargeMappingRepository mappingRepository;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void run(String... args) {
        if (ruleRepository.count() > 0) {
            return;
        }

        log.info("Seeding initial Spring seasonal surcharge rule and mappings...");

        ObjectNode condition = OBJECT_MAPPER.createObjectNode();
        condition.put("type", "date_range");
        condition.put("from", "01-20");
        condition.put("to", "02-15");

        SurchargeRule springRule = SurchargeRule.builder()
                .code("SPRING_SURCHARGE")
                .name("Phụ thu mùa xuân (Tết)")
                .description("Phụ thu dịp Tết / mùa xuân từ 20/01 đến 15/02")
                .calculationType(CalculationType.PERCENT)
                .calculationBase(CalculationBase.BASE_PRICE)
                .value(new BigDecimal("20.00"))
                .currency("VND")
                .condition(condition)
                .priority(0)
                .active(true)
                .build();

        springRule = ruleRepository.save(springRule);

        List<String> defaultModules = List.of("AIRPORT_TRANSFER", "INTERCITY", "TOUR", "SELF_DRIVE");
        for (int i = 0; i < defaultModules.size(); i++) {
            String mod = defaultModules.get(i);
            mappingRepository.save(ModuleSurchargeMapping.builder()
                    .moduleCode(mod)
                    .surchargeRule(springRule)
                    .applyOrder(0)
                    .isOptional(false)
                    .active(true)
                    .build());
        }

        log.info("Surcharge data initialized successfully with SPRING_SURCHARGE rule.");
    }
}
