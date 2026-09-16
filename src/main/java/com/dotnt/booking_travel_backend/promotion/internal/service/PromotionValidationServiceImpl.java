package com.dotnt.booking_travel_backend.promotion.internal.service;

import com.dotnt.booking_travel_backend.promotion.api.PromotionValidationService;
import com.dotnt.booking_travel_backend.promotion.api.dtos.PromotionResultDto;
import com.dotnt.booking_travel_backend.promotion.api.dtos.ValidatePromotionRequest;
import com.dotnt.booking_travel_backend.promotion.internal.entity.Promotion;
import com.dotnt.booking_travel_backend.promotion.internal.entity.PromotionGrant;
import com.dotnt.booking_travel_backend.promotion.internal.entity.PromotionScope;
import com.dotnt.booking_travel_backend.promotion.internal.repository.PromotionGrantRepository;
import com.dotnt.booking_travel_backend.promotion.internal.repository.PromotionRedemptionRepository;
import com.dotnt.booking_travel_backend.promotion.internal.repository.PromotionRepository;
import com.dotnt.booking_travel_backend.promotion.internal.scope.CartItem;
import com.dotnt.booking_travel_backend.promotion.internal.scope.ScopeResolverRegistry;
import com.dotnt.booking_travel_backend.shared.enums.DiscountType;
import com.dotnt.booking_travel_backend.shared.enums.IssuanceType;
import com.dotnt.booking_travel_backend.shared.enums.RedemptionStatus;
import com.dotnt.booking_travel_backend.shared.enums.ScopeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PromotionValidationServiceImpl implements PromotionValidationService {

    private final PromotionRepository promotionRepository;
    private final PromotionGrantRepository grantRepository;
    private final PromotionRedemptionRepository redemptionRepository;
    private final ScopeResolverRegistry scopeResolverRegistry;

    @Override
    public PromotionResultDto validateAndApplyPromotion(ValidatePromotionRequest request) {
        if (request == null || request.getCode() == null || request.getCode().isBlank()) {
            return PromotionResultDto.builder()
                    .valid(false)
                    .message("Mã giảm giá không được để trống")
                    .build();
        }

        String rawCode = request.getCode().trim();
        Promotion promotion = null;
        PromotionGrant matchedGrant = null;

        // 1. Check if the code is a unique grant code assigned to a customer
        var grantOpt = grantRepository.findByUniqueCodeIgnoreCase(rawCode);
        if (grantOpt.isPresent()) {
            matchedGrant = grantOpt.get();
            promotion = matchedGrant.getPromotion();
        } else {
            // Otherwise, look up by promotion core code
            promotion = promotionRepository.findByCodeIgnoreCaseAndActiveTrue(rawCode).orElse(null);
        }

        if (promotion == null) {
            return PromotionResultDto.builder()
                    .valid(false)
                    .message("Mã giảm giá không hợp lệ hoặc không hoạt động")
                    .build();
        }

        // 2. Check validity dates and active status
        LocalDateTime now = LocalDateTime.now();
        if (!promotion.isActive()) {
            return PromotionResultDto.builder()
                    .valid(false)
                    .message("Chương trình khuyến mãi hiện đang tạm ngưng")
                    .build();
        }

        if (promotion.getValidFrom() != null && now.isBefore(promotion.getValidFrom())) {
            return PromotionResultDto.builder()
                    .valid(false)
                    .message("Mã giảm giá chưa đến ngày áp dụng")
                    .build();
        }

        if (promotion.getValidTo() != null && now.isAfter(promotion.getValidTo())) {
            return PromotionResultDto.builder()
                    .valid(false)
                    .message("Mã giảm giá đã hết hạn sử dụng")
                    .build();
        }

        // 3. Check customer grant eligibility if not PUBLIC
        if (promotion.getIssuanceType() != IssuanceType.PUBLIC) {
            Long customerId = request.getCustomerId();
            if (customerId == null) {
                return PromotionResultDto.builder()
                        .valid(false)
                        .message("Vui lòng đăng nhập để sử dụng mã ưu đãi cá nhân này")
                        .build();
            }

            if (matchedGrant == null) {
                matchedGrant = grantRepository.findByPromotionIdAndCustomerId(promotion.getId(), customerId).orElse(null);
            }

            if (matchedGrant == null) {
                return PromotionResultDto.builder()
                        .valid(false)
                        .message("Bạn không thuộc danh sách được áp dụng mã giảm giá này")
                        .build();
            }

            if (!matchedGrant.getCustomerId().equals(customerId)) {
                return PromotionResultDto.builder()
                        .valid(false)
                        .message("Mã ưu đãi này không thuộc về tài khoản của bạn")
                        .build();
            }

            if (matchedGrant.isUsed()) {
                return PromotionResultDto.builder()
                        .valid(false)
                        .message("Bạn đã sử dụng mã giảm giá này rồi")
                        .build();
            }

            if (matchedGrant.getExpiresAt() != null && now.isAfter(matchedGrant.getExpiresAt())) {
                return PromotionResultDto.builder()
                        .valid(false)
                        .message("Mã ưu đãi được cấp cho bạn đã hết hạn sử dụng")
                        .build();
            }
        }

        // 4. Check scope matching
        List<PromotionScope> scopes = promotion.getScopes();
        boolean hasScopes = scopes != null && !scopes.isEmpty();
        boolean isGlobalScope = !hasScopes || scopes.stream().anyMatch(s -> s.getScopeType() == ScopeType.GLOBAL);

        if (!isGlobalScope) {
            // Prepare cart items to check
            List<CartItem> itemsToCheck = new ArrayList<>();
            if (request.getCartItems() != null && !request.getCartItems().isEmpty()) {
                itemsToCheck.addAll(request.getCartItems());
            } else if (request.getScopeType() != null && !request.getScopeType().isBlank()) {
                itemsToCheck.add(CartItem.builder()
                        .module(request.getScopeType())
                        .productId(request.getScopeRefId())
                        .itemPrice(request.getOrderAmount())
                        .quantity(request.getDays())
                        .build());
            }

            boolean anyScopeMatched = false;
            for (PromotionScope scope : scopes) {
                ScopeType type = scope.getScopeType();
                String refId = scope.getScopeRefId();

                if (itemsToCheck.isEmpty()) {
                    // Fallback when no item details provided
                    anyScopeMatched = true;
                    break;
                }

                for (CartItem item : itemsToCheck) {
                    try {
                        if (scopeResolverRegistry.matches(type, item, refId)) {
                            anyScopeMatched = true;
                            break;
                        }
                    } catch (Exception e) {
                        log.warn("Scope resolver check failed for type {}: {}", type, e.getMessage());
                    }
                }

                if (anyScopeMatched) break;
            }

            if (!anyScopeMatched) {
                return PromotionResultDto.builder()
                        .valid(false)
                        .message("Mã giảm giá không áp dụng cho loại dịch vụ hoặc mục này")
                        .build();
            }
        }

        // 5. Check usage limits (total & per user)
        if (promotion.getMaxUsageTotal() != null && promotion.getUsageCount() != null
                && promotion.getUsageCount() >= promotion.getMaxUsageTotal()) {
            return PromotionResultDto.builder()
                    .valid(false)
                    .message("Mã giảm giá đã hết lượt sử dụng trên hệ thống")
                    .build();
        }

        if (request.getCustomerId() != null && promotion.getMaxUsagePerUser() != null && promotion.getMaxUsagePerUser() > 0) {
            long usedByUser = redemptionRepository.countByPromotionIdAndCustomerIdAndStatus(
                    promotion.getId(), request.getCustomerId(), RedemptionStatus.APPLIED);
            if (usedByUser >= promotion.getMaxUsagePerUser()) {
                return PromotionResultDto.builder()
                        .valid(false)
                        .message("Bạn đã sử dụng hết " + promotion.getMaxUsagePerUser() + " lượt cho mã ưu đãi này")
                        .build();
            }
        }

        // 6. Check conditions (minDays, minOrderAmount)
        if (promotion.getMinDays() != null && request.getDays() != null
                && request.getDays() < promotion.getMinDays()) {
            return PromotionResultDto.builder()
                    .valid(false)
                    .message("Chỉ áp dụng khi đặt tối thiểu " + promotion.getMinDays() + " ngày")
                    .build();
        }

        BigDecimal orderAmount = request.getOrderAmount() != null ? request.getOrderAmount() : BigDecimal.ZERO;
        if (promotion.getMinOrderAmount() != null && orderAmount.compareTo(promotion.getMinOrderAmount()) < 0) {
            return PromotionResultDto.builder()
                    .valid(false)
                    .message("Chỉ áp dụng cho đơn hàng từ " + String.format("%,d", promotion.getMinOrderAmount().longValue()) + " VND")
                    .build();
        }

        // 7. Compute discount amount
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (promotion.getDiscountType() == DiscountType.PERCENT) {
            BigDecimal rate = promotion.getDiscountValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            discountAmount = orderAmount.multiply(rate).setScale(0, RoundingMode.HALF_UP);
            if (promotion.getMaxDiscountAmount() != null && discountAmount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
                discountAmount = promotion.getMaxDiscountAmount();
            }
        } else {
            discountAmount = promotion.getDiscountValue();
        }

        if (discountAmount.compareTo(orderAmount) > 0) {
            discountAmount = orderAmount;
        }

        BigDecimal finalAmount = orderAmount.subtract(discountAmount);

        return PromotionResultDto.builder()
                .valid(true)
                .promotionId(promotion.getId())
                .grantId(matchedGrant != null ? matchedGrant.getId() : null)
                .discountCode(matchedGrant != null && matchedGrant.getUniqueCode() != null ? matchedGrant.getUniqueCode() : promotion.getCode())
                .discountType(promotion.getDiscountType().name())
                .discountValue(promotion.getDiscountValue())
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .message("Áp dụng mã giảm giá thành công!")
                .build();
    }
}
