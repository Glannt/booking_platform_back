package com.dotnt.booking_travel_backend.discount.internal.controller;

import com.dotnt.booking_travel_backend.discount.api.DiscountService;
import com.dotnt.booking_travel_backend.discount.api.dtos.PriceDiscountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/discounts")
@RequiredArgsConstructor
public class AdminDiscountController {

    private final DiscountService discountService;

    @GetMapping
    public ResponseEntity<Page<PriceDiscountDto>> list(
            @RequestParam(required = false) String scopeType,
            @RequestParam(required = false) String scopeRefId,
            Pageable pageable) {
        return ResponseEntity.ok(discountService.list(scopeType, scopeRefId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PriceDiscountDto> get(@PathVariable Long id) {
        PriceDiscountDto dto = discountService.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<PriceDiscountDto> create(@RequestBody PriceDiscountDto dto) {
        return ResponseEntity.ok(discountService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<PriceDiscountDto> update(@PathVariable Long id, @RequestBody PriceDiscountDto dto) {
        PriceDiscountDto updated = discountService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        discountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
