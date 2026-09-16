package com.dotnt.booking_travel_backend.intercity.api;

import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityPriceCalculateRequest;
import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityPriceCalculateResponse;
import com.dotnt.booking_travel_backend.intercity.api.dtos.PriceListDto;
import com.dotnt.booking_travel_backend.intercity.internal.service.IIntercityPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/intercity-route-prices")
@RequiredArgsConstructor
public class IntercityRoutePriceController {

    private final IIntercityPriceService priceService;

    @GetMapping("/by-route/{routeId}")
    public ResponseEntity<List<PriceListDto>> getByRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(priceService.listByRoute(routeId));
    }

    @GetMapping
    public ResponseEntity<List<PriceListDto>> list() {
        return ResponseEntity.ok(priceService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PriceListDto> get(@PathVariable Long id) {
        PriceListDto dto = priceService.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping("/calculate")
    public ResponseEntity<IntercityPriceCalculateResponse> calculatePrice(@RequestBody IntercityPriceCalculateRequest req) {
        return ResponseEntity.ok(priceService.calculatePrice(req));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INTERCITY_PRICE_CREATE')")
    public ResponseEntity<PriceListDto> create(@RequestBody PriceListDto dto) {
        return ResponseEntity.ok(priceService.savePrice(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INTERCITY_PRICE_UPDATE')")
    public ResponseEntity<PriceListDto> update(@PathVariable Long id, @RequestBody PriceListDto dto) {
        dto.setId(id);
        return ResponseEntity.ok(priceService.savePrice(dto));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('INTERCITY_PRICE_UPDATE')")
    public ResponseEntity<PriceListDto> updateStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(priceService.updateStatus(id, active));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('INTERCITY_PRICE_DELETE', 'INTERCITY_PRICE_UPDATE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        priceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/init-defaults")
    @PreAuthorize("hasAuthority('INTERCITY_PRICE_CREATE')")
    public ResponseEntity<String> initDefaults() {
        priceService.initDefaultPrices();
        return ResponseEntity.ok("Successfully initialized standard default prices for all intercity routes.");
    }
}
