package com.dotnt.booking_travel_backend.surcharge.api;

import com.dotnt.booking_travel_backend.surcharge.api.dtos.ModuleDescriptorDto;
import com.dotnt.booking_travel_backend.surcharge.api.dtos.ModuleSurchargeMappingDto;
import com.dotnt.booking_travel_backend.surcharge.api.dtos.SurchargeRuleDto;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.CalculationBase;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.CalculationType;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.ModuleSurchargeMapping;
import com.dotnt.booking_travel_backend.surcharge.internal.domain.SurchargeRule;
import com.dotnt.booking_travel_backend.surcharge.internal.registry.ModuleRegistry;
import com.dotnt.booking_travel_backend.surcharge.internal.repository.ModuleSurchargeMappingRepository;
import com.dotnt.booking_travel_backend.surcharge.internal.repository.SurchargeRuleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/surcharge")
@RequiredArgsConstructor
public class SurchargeAdminController {

    private final SurchargeRuleRepository ruleRepository;
    private final ModuleSurchargeMappingRepository mappingRepository;
    private final ModuleRegistry moduleRegistry;
    private final ObjectMapper objectMapper;

    // ── Rules CRUD ──

    @GetMapping("/rules")
    public ResponseEntity<Page<SurchargeRuleDto>> getRules(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {
        Page<SurchargeRule> page = ruleRepository.findWithFilters(code, active, pageable);
        return ResponseEntity.ok(page.map(this::toRuleDto));
    }

    @GetMapping("/rules/{id}")
    public ResponseEntity<SurchargeRuleDto> getRuleById(@PathVariable Long id) {
        return ruleRepository.findById(id)
                .map(rule -> ResponseEntity.ok(toRuleDto(rule)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/rules")
    @PreAuthorize("hasAuthority('SURCHARGE_RULE_CREATE')")
    @Transactional
    public ResponseEntity<SurchargeRuleDto> createRule(@Valid @RequestBody SurchargeRuleDto dto) {
        SurchargeRule rule = SurchargeRule.builder()
                .code(dto.getCode().trim().toUpperCase())
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .calculationType(CalculationType.valueOf(dto.getCalculationType().toUpperCase()))
                .calculationBase(CalculationBase.valueOf(dto.getCalculationBase().toUpperCase()))
                .value(dto.getValue())
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "VND")
                .condition(parseCondition(dto.getCondition()))
                .priority(dto.getPriority() != null ? dto.getPriority() : 0)
                .effectiveFrom(dto.getEffectiveFrom())
                .effectiveTo(dto.getEffectiveTo())
                .active(dto.isActive())
                .build();

        rule = ruleRepository.save(rule);
        return ResponseEntity.ok(toRuleDto(rule));
    }

    @PutMapping("/rules/{id}")
    @PreAuthorize("hasAuthority('SURCHARGE_RULE_UPDATE')")
    @Transactional
    public ResponseEntity<SurchargeRuleDto> updateRule(@PathVariable Long id, @Valid @RequestBody SurchargeRuleDto dto) {
        return ruleRepository.findById(id).map(rule -> {
            rule.setCode(dto.getCode().trim().toUpperCase());
            rule.setName(dto.getName().trim());
            rule.setDescription(dto.getDescription());
            rule.setCalculationType(CalculationType.valueOf(dto.getCalculationType().toUpperCase()));
            rule.setCalculationBase(CalculationBase.valueOf(dto.getCalculationBase().toUpperCase()));
            rule.setValue(dto.getValue());
            rule.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "VND");
            rule.setCondition(parseCondition(dto.getCondition()));
            rule.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
            rule.setEffectiveFrom(dto.getEffectiveFrom());
            rule.setEffectiveTo(dto.getEffectiveTo());
            rule.setActive(dto.isActive());
            return ResponseEntity.ok(toRuleDto(ruleRepository.save(rule)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/rules/{id}")
    @PreAuthorize("hasAuthority('SURCHARGE_RULE_DELETE')")
    @Transactional
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        if (!ruleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // Delete all mappings associated with this rule first
        List<ModuleSurchargeMapping> mappings = mappingRepository.findBySurchargeRuleId(id);
        mappingRepository.deleteAll(mappings);
        ruleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Modules Registered ──

    @GetMapping("/modules")
    public ResponseEntity<List<ModuleDescriptorDto>> getRegisteredModules() {
        List<ModuleDescriptorDto> list = moduleRegistry.getAll().stream()
                .map(d -> new ModuleDescriptorDto(d.moduleCode(), d.moduleName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // ── Mappings CRUD ──

    @GetMapping("/mappings")
    public ResponseEntity<List<ModuleSurchargeMappingDto>> getMappings(
            @RequestParam(required = false) String moduleCode) {
        String code = (moduleCode != null && !moduleCode.isBlank()) ? moduleCode.trim().toUpperCase() : null;
        List<ModuleSurchargeMapping> list = mappingRepository.findAllWithRule(code);
        return ResponseEntity.ok(list.stream().map(this::toMappingDto).collect(Collectors.toList()));
    }

    @PostMapping("/mappings")
    @PreAuthorize("hasAuthority('SURCHARGE_MAPPING_MANAGE')")
    @Transactional
    public ResponseEntity<ModuleSurchargeMappingDto> createOrUpdateMapping(@Valid @RequestBody ModuleSurchargeMappingDto dto) {
        String moduleCode = dto.getModuleCode().trim().toUpperCase();
        SurchargeRule rule = ruleRepository.findById(dto.getSurchargeRuleId())
                .orElseThrow(() -> new IllegalArgumentException("SurchargeRule not found with id: " + dto.getSurchargeRuleId()));

        ModuleSurchargeMapping mapping = mappingRepository.findByModuleCodeAndSurchargeRuleId(moduleCode, rule.getId())
                .orElse(ModuleSurchargeMapping.builder()
                        .moduleCode(moduleCode)
                        .surchargeRule(rule)
                        .build());

        mapping.setApplyOrder(dto.getApplyOrder() != null ? dto.getApplyOrder() : 0);
        mapping.setOptional(dto.isOptional());
        mapping.setActive(dto.isActive());

        mapping = mappingRepository.save(mapping);
        return ResponseEntity.ok(toMappingDto(mapping));
    }

    @DeleteMapping("/mappings/{id}")
    @PreAuthorize("hasAuthority('SURCHARGE_MAPPING_MANAGE')")
    @Transactional
    public ResponseEntity<Void> deleteMapping(@PathVariable Long id) {
        if (!mappingRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        mappingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ──

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

    private ModuleSurchargeMappingDto toMappingDto(ModuleSurchargeMapping m) {
        return ModuleSurchargeMappingDto.builder()
                .id(m.getId())
                .moduleCode(m.getModuleCode())
                .surchargeRuleId(m.getSurchargeRule() != null ? m.getSurchargeRule().getId() : null)
                .surchargeRule(m.getSurchargeRule() != null ? toRuleDto(m.getSurchargeRule()) : null)
                .applyOrder(m.getApplyOrder())
                .isOptional(m.isOptional())
                .active(m.isActive())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }

    private JsonNode parseCondition(Object condition) {
        if (condition == null) return null;
        if (condition instanceof JsonNode jn) return jn;
        return objectMapper.valueToTree(condition);
    }

    // ponytail: Jackson 2 JsonNode serialized by Jackson 3 dumps internal getters.
    // Convert to plain Map so Jackson 3 sees a regular POJO.
    private Object toPlainCondition(JsonNode node) {
        if (node == null || node.isNull()) return null;
        try {
            return objectMapper.treeToValue(node, Object.class);
        } catch (Exception e) {
            return null;
        }
    }
}
