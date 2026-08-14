package com.dotnt.booking_travel_backend.surcharge.api;

import com.dotnt.booking_travel_backend.surcharge.api.dtos.CalculateSurchargeRequestDto;
import com.dotnt.booking_travel_backend.surcharge.api.dtos.SurchargeRuleDto;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.ModuleSurchargeMapping;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.SurchargeRule;
import com.dotnt.booking_travel_backend.surcharge.internal.repository.ModuleSurchargeMappingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/surcharge/public")
@RequiredArgsConstructor
public class SurchargePublicController {

    private final SurchargeService surchargeService;
    private final ModuleSurchargeMappingRepository mappingRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/rules")
    public ResponseEntity<List<SurchargeRuleDto>> getModuleRules(@RequestParam String moduleCode) {
        if (moduleCode == null || moduleCode.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        List<ModuleSurchargeMapping> mappings = mappingRepository.findActiveMappingsWithRule(moduleCode.trim().toUpperCase());
        List<SurchargeRuleDto> rules = mappings.stream()
                .map(ModuleSurchargeMapping::getSurchargeRule)
                .filter(r -> r != null && r.isActive())
                .map(this::toRuleDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rules);
    }

    @PostMapping("/calculate")
    public ResponseEntity<SurchargeResult> calculate(@Valid @RequestBody CalculateSurchargeRequestDto request) {
        PricingContext context = PricingContext.builder()
                .basePrice(request.getBasePrice())
                .serviceTime(request.getServiceTime())
                .zoneCode(request.getZoneCode())
                .vehicleCategoryId(request.getVehicleCategoryId())
                .routeId(request.getRouteId())
                .passengerCount(request.getPassengerCount())
                .tripType(request.getTripType())
                .customAttributes(request.getCustomAttributes())
                .build();

        SurchargeResult result = surchargeService.calculate(request.getModuleCode(), context);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/preview")
    public ResponseEntity<SurchargeResult> preview(
            @RequestParam String moduleCode,
            @RequestParam BigDecimal basePrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) Long vehicleCategoryId,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) String tripType,
            @RequestParam(required = false) String zoneCode
    ) {
        LocalDateTime serviceTime = null;
        if (date != null) {
            LocalTime localTime = LocalTime.MIDNIGHT;
            if (time != null && !time.isBlank()) {
                try {
                    localTime = LocalTime.parse(time);
                } catch (Exception ignored) {}
            }
            serviceTime = LocalDateTime.of(date, localTime);
        }

        PricingContext context = PricingContext.builder()
                .basePrice(basePrice)
                .serviceTime(serviceTime)
                .zoneCode(zoneCode)
                .vehicleCategoryId(vehicleCategoryId)
                .routeId(routeId)
                .tripType(tripType)
                .build();

        SurchargeResult result = surchargeService.calculate(moduleCode, context);
        return ResponseEntity.ok(result);
    }

    private SurchargeRuleDto toRuleDto(SurchargeRule r) {
        return SurchargeRuleDto.builder()
                .id(r.getId())
                .code(r.getCode())
                .name(r.getName())
                .description(r.getDescription())
                .calculationType(r.getCalculationType() != null ? r.getCalculationType().name() : null)
                .calculationBase(r.getCalculationBase() != null ? r.getCalculationBase().name() : null)
                .value(r.getValue())
                .currency(r.getCurrency())
                .condition(toPlainCondition(r.getCondition()))
                .priority(r.getPriority())
                .effectiveFrom(r.getEffectiveFrom())
                .effectiveTo(r.getEffectiveTo())
                .active(r.isActive())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private Object toPlainCondition(JsonNode node) {
        if (node == null || node.isNull()) return null;
        try {
            return objectMapper.treeToValue(node, Object.class);
        } catch (Exception e) {
            return null;
        }
    }
}
