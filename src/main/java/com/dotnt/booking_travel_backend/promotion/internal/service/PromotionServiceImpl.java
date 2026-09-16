package com.dotnt.booking_travel_backend.promotion.internal.service;

import com.dotnt.booking_travel_backend.promotion.api.PromotionService;
import com.dotnt.booking_travel_backend.promotion.api.dtos.*;
import com.dotnt.booking_travel_backend.promotion.internal.entity.*;
import com.dotnt.booking_travel_backend.promotion.internal.repository.*;
import com.dotnt.booking_travel_backend.promotion.internal.scope.ScopeResolverRegistry;
import com.dotnt.booking_travel_backend.shared.enums.DiscountType;
import com.dotnt.booking_travel_backend.shared.enums.GrantReason;
import com.dotnt.booking_travel_backend.shared.enums.IssuanceType;
import com.dotnt.booking_travel_backend.shared.enums.ScopeType;
import com.dotnt.booking_travel_backend.user.internal.entity.User;
import com.dotnt.booking_travel_backend.user.internal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionScopeRepository scopeRepository;
    private final PromotionGrantRepository grantRepository;
    private final PromotionRedemptionRepository redemptionRepository;
    private final ScopeResolverRegistry scopeResolverRegistry;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PromotionDto> list(String scopeType, String scopeRefId, String issuanceType, Pageable pageable) {
        Page<Promotion> pageResult = promotionRepository.findAllOrderByCreatedAtDesc(pageable);
        List<PromotionDto> dtos = pageResult.getContent().stream()
                .filter(p -> {
                    if (issuanceType != null && !issuanceType.isBlank()) {
                        if (!p.getIssuanceType().name().equalsIgnoreCase(issuanceType)) return false;
                    }
                    if (scopeType != null && !scopeType.isBlank()) {
                        boolean hasScope = p.getScopes().stream().anyMatch(s ->
                                s.getScopeType() == ScopeType.GLOBAL ||
                                s.getScopeType().name().equalsIgnoreCase(scopeType));
                        if (!hasScope) return false;
                    }
                    return true;
                })
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, pageResult.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionDto get(Long id) {
        return promotionRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionDto getByCode(String code) {
        return promotionRepository.findByCodeIgnoreCaseAndActiveTrue(code)
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    public PromotionDto create(PromotionDto dto) {
        Promotion entity = toEntity(dto);
        if (entity.getValidFrom() == null) {
            entity.setValidFrom(LocalDateTime.now());
        }

        Promotion saved = promotionRepository.save(entity);

        // Save scopes
        if (dto.getScopes() != null && !dto.getScopes().isEmpty()) {
            for (PromotionScopeDto sDto : dto.getScopes()) {
                ScopeType st = parseScopeType(sDto.getScopeType());
                String refId = st == ScopeType.GLOBAL || (sDto.getScopeRefId() != null && sDto.getScopeRefId().isBlank())
                        ? null : sDto.getScopeRefId();

                PromotionScope sc = PromotionScope.builder()
                        .promotion(saved)
                        .scopeType(st)
                        .scopeRefId(refId)
                        .build();
                saved.getScopes().add(sc);
            }
        } else {
            // Default to GLOBAL if no scope specified
            PromotionScope globalScope = PromotionScope.builder()
                    .promotion(saved)
                    .scopeType(ScopeType.GLOBAL)
                    .scopeRefId(null)
                    .build();
            saved.getScopes().add(globalScope);
        }

        saved = promotionRepository.save(saved);
        return toDto(saved);
    }

    @Override
    public PromotionDto update(Long id, PromotionDto dto) {
        Promotion existing = promotionRepository.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            existing.setCode(dto.getCode().trim().toUpperCase());
        }
        if (dto.getIssuanceType() != null) {
            existing.setIssuanceType(parseIssuanceType(dto.getIssuanceType()));
        }
        if (dto.getDiscountType() != null) {
            existing.setDiscountType(parseDiscountType(dto.getDiscountType()));
        }
        existing.setDiscountValue(dto.getDiscountValue() != null ? dto.getDiscountValue() : BigDecimal.ZERO);
        existing.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        existing.setMinDays(dto.getMinDays());
        existing.setMinOrderAmount(dto.getMinOrderAmount());
        existing.setCustomerSegment(dto.getCustomerSegment());
        existing.setStackable(dto.isStackable());
        if (dto.getPriority() != null) existing.setPriority(dto.getPriority());
        existing.setMaxUsageTotal(dto.getMaxUsageTotal());
        if (dto.getMaxUsagePerUser() != null) existing.setMaxUsagePerUser(dto.getMaxUsagePerUser());
        existing.setValidFrom(dto.getValidFrom());
        existing.setValidTo(dto.getValidTo());

        if (dto.getActive() != null) {
            existing.setActive(dto.getActive());
        } else if (dto.getStatus() != null) {
            existing.setActive("ACTIVE".equalsIgnoreCase(dto.getStatus()));
        }

        // Update scopes if provided
        if (dto.getScopes() != null) {
            existing.getScopes().clear();
            for (PromotionScopeDto sDto : dto.getScopes()) {
                ScopeType st = parseScopeType(sDto.getScopeType());
                String refId = st == ScopeType.GLOBAL || (sDto.getScopeRefId() != null && sDto.getScopeRefId().isBlank())
                        ? null : sDto.getScopeRefId();

                PromotionScope sc = PromotionScope.builder()
                        .promotion(existing)
                        .scopeType(st)
                        .scopeRefId(refId)
                        .build();
                existing.getScopes().add(sc);
            }
        }

        Promotion saved = promotionRepository.save(existing);
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        promotionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionDto> getActivePromotions(String scopeType, String scopeRefId) {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> activePromos = promotionRepository.findActivePromotions(now);

        return activePromos.stream()
                .filter(p -> p.getIssuanceType() == IssuanceType.PUBLIC)
                .filter(p -> {
                    if (scopeType == null || scopeType.isBlank()) return true;
                    return p.getScopes().stream().anyMatch(s ->
                            s.getScopeType() == ScopeType.GLOBAL ||
                            (s.getScopeType().name().equalsIgnoreCase(scopeType) &&
                             (s.getScopeRefId() == null || s.getScopeRefId().isBlank() || s.getScopeRefId().equalsIgnoreCase(scopeRefId)))
                    );
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionGrantDto> getCustomerAvailableGrants(Long customerId) {
        if (customerId == null) return Collections.emptyList();
        LocalDateTime now = LocalDateTime.now();
        List<PromotionGrant> grants = grantRepository.findAvailableGrantsForCustomer(customerId, now);
        return grants.stream().map(this::toGrantDto).collect(Collectors.toList());
    }

    @Override
    public PromotionGrantDto grantPromotion(Long promotionId, Long customerId, String reason, Integer expireDays, String customCode) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Promotion not found with id: " + promotionId));

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + customerId));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = expireDays != null && expireDays > 0 ? now.plusDays(expireDays) : promotion.getValidTo();

        GrantReason grantReason = GrantReason.MANUAL;
        if (reason != null && !reason.isBlank()) {
            try {
                grantReason = GrantReason.valueOf(reason.trim().toUpperCase());
            } catch (Exception ignored) {}
        }

        String uniqueCode = customCode;
        if (uniqueCode == null || uniqueCode.isBlank()) {
            uniqueCode = (promotion.getCode() != null ? promotion.getCode() : "VOUCHER") + "-" + customerId + "-" + (System.currentTimeMillis() % 10000);
        }

        PromotionGrant grant = PromotionGrant.builder()
                .promotion(promotion)
                .customerId(customerId)
                .uniqueCode(uniqueCode.trim().toUpperCase())
                .grantedReason(grantReason)
                .grantedAt(now)
                .expiresAt(expiresAt)
                .used(false)
                .build();

        grant = grantRepository.save(grant);
        log.info("Granted promotion {} to customer {} ({})", promotion.getName(), customer.getEmail(), uniqueCode);

        PromotionGrantDto res = toGrantDto(grant);
        res.setCustomerEmail(customer.getEmail());
        res.setCustomerName(customer.getFullname());
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionGrantDto> getGrantsByPromotionId(Long promotionId) {
        return grantRepository.findByPromotionIdOrderByGrantedAtDesc(promotionId).stream()
                .map(this::toGrantDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionGrantDto> getGrantsByCustomerId(Long customerId) {
        return grantRepository.findByCustomerIdOrderByGrantedAtDesc(customerId).stream()
                .map(this::toGrantDto)
                .collect(Collectors.toList());
    }

    private PromotionDto toDto(Promotion e) {
        List<PromotionScopeDto> scopeDtos = e.getScopes() != null
                ? e.getScopes().stream().map(s -> PromotionScopeDto.builder()
                        .id(s.getId())
                        .scopeType(s.getScopeType() != null ? s.getScopeType().name() : "GLOBAL")
                        .scopeRefId(s.getScopeRefId())
                        .build()).collect(Collectors.toList())
                : new ArrayList<>();

        List<PromotionConditionDto> condDtos = e.getConditions() != null
                ? e.getConditions().stream().map(c -> PromotionConditionDto.builder()
                        .id(c.getId())
                        .conditionType(c.getConditionType() != null ? c.getConditionType().name() : null)
                        .operator(c.getOperator())
                        .conditionValue(c.getConditionValue())
                        .build()).collect(Collectors.toList())
                : new ArrayList<>();

        return PromotionDto.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .code(e.getCode())
                .issuanceType(e.getIssuanceType() != null ? e.getIssuanceType().name() : "PUBLIC")
                .discountType(e.getDiscountType() != null ? e.getDiscountType().name() : "PERCENT")
                .discountValue(e.getDiscountValue())
                .maxDiscountAmount(e.getMaxDiscountAmount())
                .minDays(e.getMinDays())
                .minOrderAmount(e.getMinOrderAmount())
                .customerSegment(e.getCustomerSegment())
                .stackable(e.isStackable())
                .priority(e.getPriority())
                .maxUsageTotal(e.getMaxUsageTotal())
                .maxUsagePerUser(e.getMaxUsagePerUser())
                .usageCount(e.getUsageCount())
                .validFrom(e.getValidFrom())
                .validTo(e.getValidTo())
                .active(e.isActive())
                .status(e.isActive() ? "ACTIVE" : "INACTIVE")
                .scopes(scopeDtos)
                .conditions(condDtos)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private Promotion toEntity(PromotionDto d) {
        boolean active = d.getActive() != null ? d.getActive() : !"INACTIVE".equalsIgnoreCase(d.getStatus());
        return Promotion.builder()
                .name(d.getName())
                .description(d.getDescription())
                .code(d.getCode() != null ? d.getCode().trim().toUpperCase() : null)
                .issuanceType(parseIssuanceType(d.getIssuanceType()))
                .discountType(parseDiscountType(d.getDiscountType()))
                .discountValue(d.getDiscountValue() != null ? d.getDiscountValue() : BigDecimal.ZERO)
                .maxDiscountAmount(d.getMaxDiscountAmount())
                .minDays(d.getMinDays())
                .minOrderAmount(d.getMinOrderAmount())
                .customerSegment(d.getCustomerSegment())
                .stackable(d.isStackable())
                .priority(d.getPriority() != null ? d.getPriority() : 0)
                .maxUsageTotal(d.getMaxUsageTotal())
                .maxUsagePerUser(d.getMaxUsagePerUser() != null ? d.getMaxUsagePerUser() : 1)
                .usageCount(d.getUsageCount() != null ? d.getUsageCount() : 0)
                .validFrom(d.getValidFrom())
                .validTo(d.getValidTo())
                .active(active)
                .scopes(new ArrayList<>())
                .conditions(new ArrayList<>())
                .grants(new ArrayList<>())
                .build();
    }

    private PromotionGrantDto toGrantDto(PromotionGrant g) {
        String promoName = null;
        String promoCode = null;
        String discountType = null;
        java.math.BigDecimal discountValue = null;
        java.math.BigDecimal maxDiscountAmount = null;
        java.math.BigDecimal minOrderAmount = null;
        String description = null;

        if (g.getPromotion() != null) {
            promoName = g.getPromotion().getName();
            promoCode = g.getPromotion().getCode();
            discountType = g.getPromotion().getDiscountType() != null ? g.getPromotion().getDiscountType().name() : null;
            discountValue = g.getPromotion().getDiscountValue();
            maxDiscountAmount = g.getPromotion().getMaxDiscountAmount();
            minOrderAmount = g.getPromotion().getMinOrderAmount();
            description = g.getPromotion().getDescription();
        }

        String userEmail = null;
        String userName = null;
        try {
            Optional<User> uOpt = userRepository.findById(g.getCustomerId());
            if (uOpt.isPresent()) {
                userEmail = uOpt.get().getEmail();
                userName = uOpt.get().getFullname();
            }
        } catch (Exception ignored) {}

        return PromotionGrantDto.builder()
                .id(g.getId())
                .promotionId(g.getPromotion() != null ? g.getPromotion().getId() : null)
                .promotionName(promoName)
                .promotionCode(promoCode)
                .discountType(discountType)
                .discountValue(discountValue)
                .maxDiscountAmount(maxDiscountAmount)
                .minOrderAmount(minOrderAmount)
                .description(description)
                .customerId(g.getCustomerId())
                .customerEmail(userEmail)
                .customerName(userName)
                .uniqueCode(g.getUniqueCode())
                .grantedReason(g.getGrantedReason() != null ? g.getGrantedReason().name() : "MANUAL")
                .grantedAt(g.getGrantedAt())
                .expiresAt(g.getExpiresAt())
                .used(g.isUsed())
                .usedAt(g.getUsedAt())
                .usedOrderId(g.getUsedOrderId())
                .build();
    }

    private ScopeType parseScopeType(String typeStr) {
        if (typeStr == null || typeStr.isBlank()) return ScopeType.GLOBAL;
        try {
            return ScopeType.valueOf(typeStr.trim().toUpperCase());
        } catch (Exception e) {
            return ScopeType.GLOBAL;
        }
    }

    private IssuanceType parseIssuanceType(String typeStr) {
        if (typeStr == null || typeStr.isBlank()) return IssuanceType.PUBLIC;
        try {
            return IssuanceType.valueOf(typeStr.trim().toUpperCase());
        } catch (Exception e) {
            return IssuanceType.PUBLIC;
        }
    }

    private DiscountType parseDiscountType(String typeStr) {
        if (typeStr == null || typeStr.isBlank()) return DiscountType.PERCENT;
        try {
            return DiscountType.valueOf(typeStr.trim().toUpperCase());
        } catch (Exception e) {
            return DiscountType.PERCENT;
        }
    }
}
