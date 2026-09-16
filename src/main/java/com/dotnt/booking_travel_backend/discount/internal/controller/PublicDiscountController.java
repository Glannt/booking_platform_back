package com.dotnt.booking_travel_backend.discount.internal.controller;

import com.dotnt.booking_travel_backend.discount.api.DiscountService;
import com.dotnt.booking_travel_backend.discount.api.dtos.DiscountResultDto;
import com.dotnt.booking_travel_backend.discount.api.dtos.PriceDiscountDto;
import com.dotnt.booking_travel_backend.discount.api.dtos.ValidateDiscountRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/discounts", "/api/v1/public/discounts"})
@RequiredArgsConstructor
public class PublicDiscountController {

    private final DiscountService discountService;

    @PostMapping("/validate")
    public ResponseEntity<DiscountResultDto> validateDiscount(@RequestBody ValidateDiscountRequest request) {
        return ResponseEntity.ok(discountService.validateAndApplyDiscount(request));
    }

    @GetMapping("/active")
    public ResponseEntity<java.util.List<PriceDiscountDto>> getActiveDiscounts(
            @RequestParam(required = false) String scopeType,
            @RequestParam(required = false) String scopeRefId) {
        return ResponseEntity.ok(discountService.getActiveDiscounts(scopeType, scopeRefId));
    }
}
