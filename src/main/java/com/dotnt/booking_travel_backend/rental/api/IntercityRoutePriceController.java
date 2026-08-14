package com.dotnt.booking_travel_backend.rental.api;

import com.dotnt.booking_travel_backend.rental.api.dtos.IntercityRoutePriceDto;
import com.dotnt.booking_travel_backend.rental.internal.repository.IntercityRoutePriceRepository;
import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityRoutePrice;
import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityRoute;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/intercity-route-prices")
@RequiredArgsConstructor
public class IntercityRoutePriceController {

    private final IntercityRoutePriceRepository priceRepository;

    @GetMapping("/by-route/{routeId}")
    public ResponseEntity<List<IntercityRoutePriceDto>> getByRoute(@PathVariable Long routeId) {
        List<IntercityRoutePriceDto> prices = priceRepository.findByRouteIdAndActiveTrue(routeId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(prices);
    }

    @GetMapping
    public ResponseEntity<List<IntercityRoutePriceDto>> list() {
        List<IntercityRoutePriceDto> prices = priceRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(prices);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INTERCITY_PRICE_CREATE')")
    public ResponseEntity<IntercityRoutePriceDto> create(@RequestBody IntercityRoutePriceDto dto) {
        IntercityRoute route = new IntercityRoute();
        route.setId(dto.getRouteId());

        IntercityRoutePrice price = IntercityRoutePrice.builder()
                .route(route)
                .vehicleCategoryId(dto.getVehicleCategoryId())
                .perKmAmount(dto.getPerKmAmount())
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "VND")
                .active(dto.isActive())
                .build();
        price = priceRepository.save(price);
        return ResponseEntity.ok(toDto(price));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INTERCITY_PRICE_UPDATE')")
    public ResponseEntity<IntercityRoutePriceDto> update(@PathVariable Long id, @RequestBody IntercityRoutePriceDto dto) {
        return priceRepository.findById(id).map(price -> {
            price.setVehicleCategoryId(dto.getVehicleCategoryId());
            price.setPerKmAmount(dto.getPerKmAmount());
            price.setCurrency(dto.getCurrency());
            price.setActive(dto.isActive());
            return ResponseEntity.ok(toDto(priceRepository.save(price)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INTERCITY_PRICE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        priceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private IntercityRoutePriceDto toDto(IntercityRoutePrice p) {
        return IntercityRoutePriceDto.builder()
                .id(p.getId())
                .routeId(p.getRoute() != null ? p.getRoute().getId() : null)
                .vehicleCategoryId(p.getVehicleCategoryId())
                .perKmAmount(p.getPerKmAmount())
                .currency(p.getCurrency())
                .active(p.isActive())
                .build();
    }
}
