package com.dotnt.booking_travel_backend.promotion.internal.controller;

import com.dotnt.booking_travel_backend.promotion.api.PromotionService;
import com.dotnt.booking_travel_backend.promotion.api.PromotionValidationService;
import com.dotnt.booking_travel_backend.promotion.api.dtos.PromotionDto;
import com.dotnt.booking_travel_backend.promotion.api.dtos.PromotionGrantDto;
import com.dotnt.booking_travel_backend.promotion.api.dtos.PromotionResultDto;
import com.dotnt.booking_travel_backend.promotion.api.dtos.ValidatePromotionRequest;
import com.dotnt.booking_travel_backend.user.api.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/promotions", "/api/v1/public/promotions"})
@RequiredArgsConstructor
public class PublicPromotionController {

    private final PromotionValidationService validationService;
    private final PromotionService promotionService;
    private final UserFacade userFacade;

    @PostMapping("/validate")
    public ResponseEntity<PromotionResultDto> validatePromotion(@RequestBody ValidatePromotionRequest request) {
        if (request.getCustomerId() == null) {
            Long resolvedUserId = resolveCurrentUserId();
            if (resolvedUserId != null) {
                request.setCustomerId(resolvedUserId);
            }
        }
        return ResponseEntity.ok(validationService.validateAndApplyPromotion(request));
    }

    @GetMapping("/active")
    public ResponseEntity<List<PromotionDto>> getActivePromotions(
            @RequestParam(required = false) String scopeType,
            @RequestParam(required = false) String scopeRefId) {
        return ResponseEntity.ok(promotionService.getActivePromotions(scopeType, scopeRefId));
    }

    @GetMapping("/my-vouchers")
    public ResponseEntity<List<PromotionGrantDto>> getMyVouchers() {
        Long userId = resolveCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(promotionService.getCustomerAvailableGrants(userId));
    }

    private Long resolveCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof String email && !"anonymousUser".equals(email)) {
                return userFacade.findUserIdByEmail(email);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
