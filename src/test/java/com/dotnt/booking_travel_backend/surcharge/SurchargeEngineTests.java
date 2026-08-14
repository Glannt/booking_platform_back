package com.dotnt.booking_travel_backend.surcharge;

import com.dotnt.booking_travel_backend.surcharge.api.PricingContext;
import com.dotnt.booking_travel_backend.surcharge.api.SurchargeLineItem;
import com.dotnt.booking_travel_backend.surcharge.api.SurchargeResult;
import com.dotnt.booking_travel_backend.surcharge.internal.SurchargeServiceImpl;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.CalculationBase;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.CalculationType;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.ModuleSurchargeMapping;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.SurchargeRule;
import com.dotnt.booking_travel_backend.surcharge.internal.engine.*;
import com.dotnt.booking_travel_backend.surcharge.internal.registry.ModuleRegistry;
import com.dotnt.booking_travel_backend.surcharge.internal.repository.ModuleSurchargeMappingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SurchargeEngineTests {

    private ObjectMapper mapper;
    private TimeRangeConditionEvaluator timeRangeEvaluator;
    private DateRangeConditionEvaluator dateRangeEvaluator;
    private DayOfWeekConditionEvaluator dayOfWeekEvaluator;
    private ZoneConditionEvaluator zoneEvaluator;
    private VehicleCategoryConditionEvaluator vehicleEvaluator;
    private RouteConditionEvaluator routeEvaluator;
    private AndConditionEvaluator andEvaluator;
    private OrConditionEvaluator orEvaluator;
    private ConditionEvaluatorFactory factory;
    private SurchargeCalculator calculator;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        timeRangeEvaluator = new TimeRangeConditionEvaluator();
        dateRangeEvaluator = new DateRangeConditionEvaluator();
        dayOfWeekEvaluator = new DayOfWeekConditionEvaluator();
        zoneEvaluator = new ZoneConditionEvaluator();
        vehicleEvaluator = new VehicleCategoryConditionEvaluator();
        routeEvaluator = new RouteConditionEvaluator();

        List<ConditionEvaluator> evaluators = List.of(
                timeRangeEvaluator,
                dateRangeEvaluator,
                dayOfWeekEvaluator,
                zoneEvaluator,
                vehicleEvaluator,
                routeEvaluator
        );
        factory = new ConditionEvaluatorFactory(evaluators);
        andEvaluator = new AndConditionEvaluator(factory);
        orEvaluator = new OrConditionEvaluator(factory);
        evaluators = List.of(
                timeRangeEvaluator,
                dateRangeEvaluator,
                dayOfWeekEvaluator,
                zoneEvaluator,
                vehicleEvaluator,
                routeEvaluator,
                andEvaluator,
                orEvaluator
        );
        factory = new ConditionEvaluatorFactory(evaluators);
        calculator = new SurchargeCalculator();
    }

    @Test
    void testTimeRangeOvernight() {
        ObjectNode cond = mapper.createObjectNode();
        cond.put("type", "time_range");
        cond.put("from", "22:00");
        cond.put("to", "05:00");

        PricingContext ctxNight = PricingContext.builder()
                .serviceTime(LocalDateTime.of(2026, 8, 14, 23, 30))
                .build();
        assertTrue(timeRangeEvaluator.evaluate(cond, ctxNight));

        PricingContext ctxEarlyMorning = PricingContext.builder()
                .serviceTime(LocalDateTime.of(2026, 8, 14, 4, 30))
                .build();
        assertTrue(timeRangeEvaluator.evaluate(cond, ctxEarlyMorning));

        PricingContext ctxDay = PricingContext.builder()
                .serviceTime(LocalDateTime.of(2026, 8, 14, 14, 0))
                .build();
        assertFalse(timeRangeEvaluator.evaluate(cond, ctxDay));
    }

    @Test
    void testDateRangeSeasonal() {
        ObjectNode cond = mapper.createObjectNode();
        cond.put("type", "date_range");
        cond.put("from", "01-20");
        cond.put("to", "02-15");

        PricingContext ctxInSeason = PricingContext.builder()
                .serviceTime(LocalDateTime.of(2026, 1, 25, 10, 0))
                .build();
        assertTrue(dateRangeEvaluator.evaluate(cond, ctxInSeason));

        PricingContext ctxOutOfSeason = PricingContext.builder()
                .serviceTime(LocalDateTime.of(2026, 5, 1, 10, 0))
                .build();
        assertFalse(dateRangeEvaluator.evaluate(cond, ctxOutOfSeason));
    }

    @Test
    void testCompositeAndCondition() {
        ObjectNode andCond = mapper.createObjectNode();
        andCond.put("type", "and");
        ArrayNode conditions = andCond.putArray("conditions");

        ObjectNode timeCond = mapper.createObjectNode();
        timeCond.put("type", "time_range");
        timeCond.put("from", "22:00");
        timeCond.put("to", "05:00");
        conditions.add(timeCond);

        ObjectNode zoneCond = mapper.createObjectNode();
        zoneCond.put("type", "zone");
        zoneCond.put("zone", "REMOTE_ZONE");
        conditions.add(zoneCond);

        PricingContext matchCtx = PricingContext.builder()
                .serviceTime(LocalDateTime.of(2026, 8, 14, 23, 0))
                .zoneCode("REMOTE_ZONE")
                .build();
        assertTrue(andEvaluator.evaluate(andCond, matchCtx));

        PricingContext nonMatchCtx = PricingContext.builder()
                .serviceTime(LocalDateTime.of(2026, 8, 14, 23, 0))
                .zoneCode("CENTER_ZONE")
                .build();
        assertFalse(andEvaluator.evaluate(andCond, nonMatchCtx));
    }

    @Test
    void testCalculatorPercentAndRunningTotal() {
        SurchargeRule rule1 = SurchargeRule.builder()
                .code("NIGHT_FEE")
                .name("Night Fee")
                .calculationType(CalculationType.PERCENT)
                .calculationBase(CalculationBase.BASE_PRICE)
                .value(new BigDecimal("10.00"))
                .build();
        rule1.setId(1L);

        BigDecimal basePrice = new BigDecimal("100000");
        SurchargeLineItem item1 = calculator.apply(rule1, basePrice, basePrice);
        assertEquals(new BigDecimal("10000.00"), item1.getAmount());

        BigDecimal runningTotal = basePrice.add(item1.getAmount()); // 110,000

        SurchargeRule rule2 = SurchargeRule.builder()
                .code("WEEKEND_SURCHARGE")
                .name("Weekend Surcharge")
                .calculationType(CalculationType.PERCENT)
                .calculationBase(CalculationBase.RUNNING_TOTAL)
                .value(new BigDecimal("10.00"))
                .build();
        rule2.setId(2L);

        SurchargeLineItem item2 = calculator.apply(rule2, basePrice, runningTotal);
        assertEquals(new BigDecimal("11000.00"), item2.getAmount()); // 10% of 110,000
    }

    @Test
    void testSurchargeServiceImplPipeline() {
        ModuleSurchargeMappingRepository mockRepo = Mockito.mock(ModuleSurchargeMappingRepository.class);
        ModuleRegistry mockRegistry = Mockito.mock(ModuleRegistry.class);
        when(mockRegistry.exists("AIRPORT_TRANSFER")).thenReturn(true);

        ObjectNode cond = mapper.createObjectNode();
        cond.put("type", "date_range");
        cond.put("from", "01-20");
        cond.put("to", "02-15");

        SurchargeRule rule = SurchargeRule.builder()
                .code("SPRING_SURCHARGE")
                .name("Spring Surcharge")
                .calculationType(CalculationType.PERCENT)
                .calculationBase(CalculationBase.BASE_PRICE)
                .value(new BigDecimal("20.00"))
                .condition(cond)
                .active(true)
                .build();
        rule.setId(1L);

        ModuleSurchargeMapping mapping = ModuleSurchargeMapping.builder()
                .moduleCode("AIRPORT_TRANSFER")
                .surchargeRule(rule)
                .applyOrder(0)
                .active(true)
                .build();

        when(mockRepo.findActiveMappingsWithRule("AIRPORT_TRANSFER")).thenReturn(List.of(mapping));

        SurchargeServiceImpl service = new SurchargeServiceImpl(mockRepo, factory, calculator, mockRegistry);

        PricingContext ctx = PricingContext.builder()
                .basePrice(new BigDecimal("500000"))
                .serviceTime(LocalDateTime.of(2026, 1, 25, 10, 0))
                .build();

        SurchargeResult result = service.calculate("AIRPORT_TRANSFER", ctx);
        assertEquals(1, result.getLineItems().size());
        assertEquals(new BigDecimal("100000.00"), result.getTotalSurcharge());
        assertEquals(new BigDecimal("600000.00"), result.getFinalAmount());
    }
}
