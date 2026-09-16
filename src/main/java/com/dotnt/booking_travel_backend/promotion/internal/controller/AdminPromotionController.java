package com.dotnt.booking_travel_backend.promotion.internal.controller;

import com.dotnt.booking_travel_backend.promotion.api.PromotionRedemptionService;
import com.dotnt.booking_travel_backend.promotion.api.PromotionService;
import com.dotnt.booking_travel_backend.promotion.api.dtos.PromotionDto;
import com.dotnt.booking_travel_backend.promotion.api.dtos.PromotionGrantDto;
import com.dotnt.booking_travel_backend.promotion.api.dtos.PromotionRedemptionDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/promotions")
@RequiredArgsConstructor
public class AdminPromotionController {

    private final PromotionService promotionService;
    private final PromotionRedemptionService redemptionService;

    @GetMapping
    public ResponseEntity<Page<PromotionDto>> list(
            @RequestParam(required = false) String scopeType,
            @RequestParam(required = false) String scopeRefId,
            @RequestParam(required = false) String issuanceType,
            Pageable pageable) {
        return ResponseEntity.ok(promotionService.list(scopeType, scopeRefId, issuanceType, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionDto> get(@PathVariable Long id) {
        PromotionDto dto = promotionService.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<PromotionDto> create(@RequestBody PromotionDto dto) {
        return ResponseEntity.ok(promotionService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<PromotionDto> update(@PathVariable Long id, @RequestBody PromotionDto dto) {
        PromotionDto updated = promotionService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promotionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/grants")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<PromotionGrantDto> grantToCustomer(
            @PathVariable Long id,
            @RequestBody GrantPromotionRequest req) {
        return ResponseEntity.ok(promotionService.grantPromotion(
                id, req.getCustomerId(), req.getReason(), req.getExpireDays(), req.getCustomCode()));
    }

    @GetMapping("/{id}/grants")
    public ResponseEntity<List<PromotionGrantDto>> getGrants(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getGrantsByPromotionId(id));
    }

    @GetMapping("/{id}/redemptions")
    public ResponseEntity<List<PromotionRedemptionDto>> getRedemptions(@PathVariable Long id) {
        return ResponseEntity.ok(redemptionService.getRedemptionsByPromotionId(id));
    }

    @Data
    public static class GrantPromotionRequest {
        private Long customerId;
        private String reason;
        private Integer expireDays;
        private String customCode;
    }
}
