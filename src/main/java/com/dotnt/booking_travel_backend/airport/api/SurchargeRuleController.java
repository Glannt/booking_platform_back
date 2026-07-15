package com.dotnt.booking_travel_backend.airport.api;

import com.dotnt.booking_travel_backend.airport.internal.entity.SurchargeRule;
import com.dotnt.booking_travel_backend.airport.internal.repository.SurchargeRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/surcharge-rules")
@RequiredArgsConstructor
public class SurchargeRuleController {
    private final SurchargeRuleRepository repository;

    @PostMapping
    @PreAuthorize("hasAuthority('SURCHARGE_CREATE')")
    public ResponseEntity<SurchargeRule> create(@RequestBody SurchargeRule dto) {
        dto.setId(null);
        dto.setVehicles(null);
        return ResponseEntity.ok(repository.save(dto));
    }

    @GetMapping
    public ResponseEntity<Page<SurchargeRule>> list(Pageable pageable) {
        return ResponseEntity.ok(repository.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SurchargeRule> get(@PathVariable Long id) {
        return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SURCHARGE_UPDATE')")
    public ResponseEntity<SurchargeRule> update(@PathVariable Long id, @RequestBody SurchargeRule dto) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        dto.setId(id);
        dto.setVehicles(null);
        return ResponseEntity.ok(repository.save(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SURCHARGE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-country/{country}")
    public ResponseEntity<List<SurchargeRule>> getByCountry(@PathVariable String country) {
        return ResponseEntity.ok(repository.findByCountryAndActiveTrue(country));
    }
}
