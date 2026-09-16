package com.dotnt.booking_travel_backend.promotion.internal.service;

import com.dotnt.booking_travel_backend.discount.internal.repository.PriceDiscountRepository;
import com.dotnt.booking_travel_backend.promotion.internal.entity.Promotion;
import com.dotnt.booking_travel_backend.promotion.internal.entity.PromotionScope;
import com.dotnt.booking_travel_backend.promotion.internal.repository.PromotionRepository;
import com.dotnt.booking_travel_backend.promotion.internal.repository.PromotionScopeRepository;
import com.dotnt.booking_travel_backend.shared.entity.PriceDiscount;
import com.dotnt.booking_travel_backend.shared.enums.DiscountType;
import com.dotnt.booking_travel_backend.shared.enums.IssuanceType;
import com.dotnt.booking_travel_backend.shared.enums.ScopeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Automatically migrates existing records from price_discounts into promotions & promotion_scopes
 * when the promotions table is empty on startup.
 */
@Component
@Order(10)
@RequiredArgsConstructor
@Slf4j
public class PromotionMigrationRunner implements CommandLineRunner {

    private final PromotionRepository promotionRepository;
    private final PromotionScopeRepository promotionScopeRepository;
    private final PriceDiscountRepository priceDiscountRepository;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            long promoCount = promotionRepository.count();
            if (promoCount > 0) {
                log.info("Promotions table already contains {} records. Skipping migration.", promoCount);
                return;
            }

            List<PriceDiscount> existingDiscounts = priceDiscountRepository.findAll();
            if (existingDiscounts.isEmpty()) {
                log.info("No legacy PriceDiscount records to migrate.");
                return;
            }

            log.info("Starting automatic migration of {} legacy PriceDiscount records to promotions...", existingDiscounts.size());

            for (PriceDiscount pd : existingDiscounts) {
                DiscountType dt = "FIXED_AMOUNT".equalsIgnoreCase(pd.getDiscountType()) ? DiscountType.FIXED_AMOUNT : DiscountType.PERCENT;

                Promotion promo = Promotion.builder()
                        .name(pd.getName() != null ? pd.getName() : pd.getCode())
                        .description(pd.getDescription())
                        .code(pd.getCode() != null ? pd.getCode().trim().toUpperCase() : null)
                        .issuanceType(IssuanceType.PUBLIC)
                        .discountType(dt)
                        .discountValue(pd.getDiscountValue())
                        .minDays(pd.getMinDays())
                        .minOrderAmount(pd.getMinOrderAmount())
                        .customerSegment(pd.getCustomerSegment())
                        .maxUsageTotal(pd.getMaxUsage())
                        .maxUsagePerUser(1)
                        .usageCount(pd.getUsageCount() != null ? pd.getUsageCount() : 0)
                        .validFrom(pd.getValidFrom() != null ? pd.getValidFrom() : LocalDateTime.now())
                        .validTo(pd.getValidTo())
                        .active(pd.isActive())
                        .build();

                promo = promotionRepository.save(promo);

                ScopeType st = ScopeType.GLOBAL;
                if (pd.getScopeType() != null && !pd.getScopeType().isBlank()) {
                    try {
                        st = ScopeType.valueOf(pd.getScopeType().trim().toUpperCase());
                    } catch (Exception ignored) {}
                }

                String refId = pd.getScopeRefId();
                if (st == ScopeType.GLOBAL || (refId != null && refId.isBlank())) {
                    refId = null;
                }

                PromotionScope scope = PromotionScope.builder()
                        .promotion(promo)
                        .scopeType(st)
                        .scopeRefId(refId)
                        .build();

                promotionScopeRepository.save(scope);
            }

            log.info("Successfully migrated {} legacy PriceDiscount records to promotions!", existingDiscounts.size());
        } catch (Exception e) {
            log.warn("Promotion migration runner encountered an exception: {}", e.getMessage());
        }
    }
}
