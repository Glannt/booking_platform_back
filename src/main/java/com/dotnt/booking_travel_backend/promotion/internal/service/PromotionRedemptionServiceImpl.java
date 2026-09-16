package com.dotnt.booking_travel_backend.promotion.internal.service;

import com.dotnt.booking_travel_backend.promotion.api.PromotionRedemptionService;
import com.dotnt.booking_travel_backend.promotion.api.dtos.PromotionRedemptionDto;
import com.dotnt.booking_travel_backend.promotion.internal.entity.Promotion;
import com.dotnt.booking_travel_backend.promotion.internal.entity.PromotionGrant;
import com.dotnt.booking_travel_backend.promotion.internal.entity.PromotionRedemption;
import com.dotnt.booking_travel_backend.promotion.internal.repository.PromotionGrantRepository;
import com.dotnt.booking_travel_backend.promotion.internal.repository.PromotionRedemptionRepository;
import com.dotnt.booking_travel_backend.promotion.internal.repository.PromotionRepository;
import com.dotnt.booking_travel_backend.shared.enums.RedemptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PromotionRedemptionServiceImpl implements PromotionRedemptionService {

    private final PromotionRepository promotionRepository;
    private final PromotionGrantRepository grantRepository;
    private final PromotionRedemptionRepository redemptionRepository;

    @Override
    public PromotionRedemptionDto redeemPromotion(Long promotionId, Long grantId, Long customerId, Long orderId, BigDecimal discountAmount) {
        if (promotionId == null || orderId == null) {
            throw new IllegalArgumentException("Promotion ID and Order ID must not be null");
        }

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Promotion not found with id: " + promotionId));

        // 1. Atomic usage count increment
        int affected = promotionRepository.incrementUsageCountAtomic(promotionId);
        if (affected == 0) {
            throw new IllegalStateException("Mã giảm giá đã hết lượt sử dụng trên hệ thống");
        }

        // 2. Mark grant as used if applicable
        PromotionGrant grant = null;
        if (grantId != null) {
            grant = grantRepository.findById(grantId).orElse(null);
            if (grant != null) {
                grant.setUsed(true);
                grant.setUsedAt(LocalDateTime.now());
                grant.setUsedOrderId(orderId);
                grant = grantRepository.save(grant);
            }
        }

        // 3. Insert redemption record
        PromotionRedemption redemption = PromotionRedemption.builder()
                .promotion(promotion)
                .grant(grant)
                .customerId(customerId != null ? customerId : 0L)
                .orderId(orderId)
                .discountAmount(discountAmount != null ? discountAmount : BigDecimal.ZERO)
                .status(RedemptionStatus.APPLIED)
                .redeemedAt(LocalDateTime.now())
                .build();

        redemption = redemptionRepository.save(redemption);
        log.info("Redeemed promotion {} for order {} (amount: {})", promotion.getCode(), orderId, discountAmount);

        return toDto(redemption);
    }

    @Override
    public void reverseRedemption(Long orderId) {
        if (orderId == null) return;

        List<PromotionRedemption> redemptions = redemptionRepository.findByOrderId(orderId);
        for (PromotionRedemption r : redemptions) {
            if (r.getStatus() == RedemptionStatus.APPLIED) {
                r.setStatus(RedemptionStatus.REVERSED);
                r.setReversedAt(LocalDateTime.now());
                redemptionRepository.save(r);

                // Decrement usage count
                promotionRepository.decrementUsageCountAtomic(r.getPromotion().getId());

                // Reset grant if applicable
                if (r.getGrant() != null) {
                    PromotionGrant g = r.getGrant();
                    g.setUsed(false);
                    g.setUsedAt(null);
                    g.setUsedOrderId(null);
                    grantRepository.save(g);
                }

                log.info("Reversed promotion redemption for order {} on promo {}", orderId, r.getPromotion().getCode());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionRedemptionDto> getRedemptionsByOrderId(Long orderId) {
        return redemptionRepository.findByOrderId(orderId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionRedemptionDto> getRedemptionsByPromotionId(Long promotionId) {
        return redemptionRepository.findByPromotionIdOrderByRedeemedAtDesc(promotionId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private PromotionRedemptionDto toDto(PromotionRedemption r) {
        return PromotionRedemptionDto.builder()
                .id(r.getId())
                .promotionId(r.getPromotion() != null ? r.getPromotion().getId() : null)
                .promotionCode(r.getPromotion() != null ? r.getPromotion().getCode() : null)
                .grantId(r.getGrant() != null ? r.getGrant().getId() : null)
                .customerId(r.getCustomerId())
                .orderId(r.getOrderId())
                .discountAmount(r.getDiscountAmount())
                .status(r.getStatus() != null ? r.getStatus().name() : null)
                .redeemedAt(r.getRedeemedAt())
                .reversedAt(r.getReversedAt())
                .build();
    }
}
