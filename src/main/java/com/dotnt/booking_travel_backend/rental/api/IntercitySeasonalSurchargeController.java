package com.dotnt.booking_travel_backend.rental.api;

import com.dotnt.booking_travel_backend.rental.api.dtos.IntercitySeasonalSurchargeDto;
import com.dotnt.booking_travel_backend.rental.internal.entity.IntercitySeasonalSurcharge;
import com.dotnt.booking_travel_backend.rental.internal.repository.IntercitySeasonalSurchargeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/intercity-surcharges")
@RequiredArgsConstructor
public class IntercitySeasonalSurchargeController {

    private final IntercitySeasonalSurchargeRepository surchargeRepository;

    @PostMapping
    @PreAuthorize("hasAuthority('INTERCITY_SURCHARGE_CREATE')")
    public ResponseEntity<IntercitySeasonalSurchargeDto> create(@RequestBody IntercitySeasonalSurchargeDto dto) {
        IntercitySeasonalSurcharge entity = IntercitySeasonalSurcharge.builder()
                .routeId(dto.getRouteId())
                .seasonName(dto.getSeasonName())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .surchargePercent(dto.getSurchargePercent())
                .active(dto.isActive())
                .build();
        entity = surchargeRepository.save(entity);
        return ResponseEntity.ok(toDto(entity));
    }

    @GetMapping
    public ResponseEntity<Page<IntercitySeasonalSurchargeDto>> list(Pageable pageable) {
        Page<IntercitySeasonalSurcharge> page = surchargeRepository.findAll(pageable);
        return ResponseEntity.ok(page.map(this::toDto));
    }

    @GetMapping("/active")
    public ResponseEntity<List<IntercitySeasonalSurchargeDto>> findActive(@RequestParam Long routeId, @RequestParam LocalDate date) {
        List<IntercitySeasonalSurchargeDto> results = surchargeRepository.findActiveByDateAndRoute(date, routeId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IntercitySeasonalSurchargeDto> get(@PathVariable Long id) {
        return surchargeRepository.findById(id).map(e -> ResponseEntity.ok(toDto(e))).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INTERCITY_SURCHARGE_UPDATE')")
    public ResponseEntity<IntercitySeasonalSurchargeDto> update(@PathVariable Long id, @RequestBody IntercitySeasonalSurchargeDto dto) {
        return surchargeRepository.findById(id).map(entity -> {
            entity.setRouteId(dto.getRouteId());
            entity.setSeasonName(dto.getSeasonName());
            entity.setStartDate(dto.getStartDate());
            entity.setEndDate(dto.getEndDate());
            entity.setSurchargePercent(dto.getSurchargePercent());
            entity.setActive(dto.isActive());
            return ResponseEntity.ok(toDto(surchargeRepository.save(entity)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INTERCITY_SURCHARGE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        surchargeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private IntercitySeasonalSurchargeDto toDto(IntercitySeasonalSurcharge s) {
        return IntercitySeasonalSurchargeDto.builder()
                .id(s.getId()).routeId(s.getRouteId()).seasonName(s.getSeasonName())
                .startDate(s.getStartDate()).endDate(s.getEndDate())
                .surchargePercent(s.getSurchargePercent()).active(s.isActive())
                .build();
    }
}
