package com.dotnt.booking_travel_backend.intercity.api;

import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityPriceCalculateRequest;
import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityPriceCalculateResponse;
import com.dotnt.booking_travel_backend.intercity.api.dtos.PriceListDto;
import com.dotnt.booking_travel_backend.intercity.internal.service.IIntercityPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/v1/public/intercity-route-prices", "/api/v1/public/intercity_route_price"})
@RequiredArgsConstructor
public class PublicIntercityRoutePriceController {

    private final IIntercityPriceService priceService;

    @GetMapping
    public ResponseEntity<List<PriceListDto>> list(
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
        List<PriceListDto> list = (routeId != null)
                ? priceService.listByRoute(routeId)
                : priceService.listAll();
        if (activeOnly) {
            list = list.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getActive()) || "ACTIVE".equalsIgnoreCase(p.getStatus()))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/by-route/{routeId}")
    public ResponseEntity<List<PriceListDto>> getByRoute(
            @PathVariable Long routeId,
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
        List<PriceListDto> list = priceService.listByRoute(routeId);
        if (activeOnly) {
            list = list.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getActive()) || "ACTIVE".equalsIgnoreCase(p.getStatus()))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping("/calculate")
    public ResponseEntity<IntercityPriceCalculateResponse> calculatePrice(@RequestBody IntercityPriceCalculateRequest req) {
        return ResponseEntity.ok(priceService.calculatePrice(req));
    }
}
