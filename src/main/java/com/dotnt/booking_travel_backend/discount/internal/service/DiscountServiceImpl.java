package com.dotnt.booking_travel_backend.discount.internal.service;

import com.dotnt.booking_travel_backend.discount.api.DiscountService;
import com.dotnt.booking_travel_backend.discount.api.dtos.DiscountResultDto;
import com.dotnt.booking_travel_backend.discount.api.dtos.PriceDiscountDto;
import com.dotnt.booking_travel_backend.discount.api.dtos.ValidateDiscountRequest;
import com.dotnt.booking_travel_backend.discount.internal.repository.PriceDiscountRepository;
import com.dotnt.booking_travel_backend.shared.entity.PriceDiscount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DiscountServiceImpl implements DiscountService {

    private final PriceDiscountRepository discountRepository;

    @Override
    @Transactional(readOnly = true)
    public DiscountResultDto validateAndApplyDiscount(ValidateDiscountRequest request) {
        if (request == null || request.getCode() == null || request.getCode().isBlank()) {
            return DiscountResultDto.builder()
                    .valid(false)
                    .message("Mã giảm giá không được để trống")
                    .build();
        }

        PriceDiscount discount = discountRepository.findByCodeIgnoreCaseAndActiveTrue(request.getCode().trim())
                .orElse(null);

        if (discount == null) {
            return DiscountResultDto.builder()
                    .valid(false)
                    .message("Mã giảm giá không hợp lệ hoặc không hoạt động")
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        if (discount.getValidFrom() != null && now.isBefore(discount.getValidFrom())) {
            return DiscountResultDto.builder()
                    .valid(false)
                    .message("Mã giảm giá chưa đến ngày áp dụng")
                    .build();
        }

        if (discount.getValidTo() != null && now.isAfter(discount.getValidTo())) {
            return DiscountResultDto.builder()
                    .valid(false)
                    .message("Mã giảm giá đã hết hạn sử dụng")
                    .build();
        }

        if (discount.getMaxUsage() != null && discount.getUsageCount() != null
                && discount.getUsageCount() >= discount.getMaxUsage()) {
            return DiscountResultDto.builder()
                    .valid(false)
                    .message("Mã giảm giá đã hết lượt sử dụng")
                    .build();
        }

        boolean isGlobal = discount.getScopeType() == null || "GLOBAL".equalsIgnoreCase(discount.getScopeType());

        if (!isGlobal && request.getScopeType() != null
                && !discount.getScopeType().equalsIgnoreCase(request.getScopeType())) {
            return DiscountResultDto.builder()
                    .valid(false)
                    .message("Mã giảm giá không áp dụng cho loại dịch vụ này")
                    .build();
        }

        if (!isGlobal && discount.getScopeRefId() != null && !discount.getScopeRefId().isBlank()
                && request.getScopeRefId() != null
                && !discount.getScopeRefId().equalsIgnoreCase(request.getScopeRefId())) {
            return DiscountResultDto.builder()
                    .valid(false)
                    .message("Mã giảm giá không áp dụng cho mục này")
                    .build();
        }

        if (discount.getMinDays() != null && request.getDays() != null
                && request.getDays() < discount.getMinDays()) {
            return DiscountResultDto.builder()
                    .valid(false)
                    .message("Chỉ áp dụng khi đặt tối thiểu " + discount.getMinDays() + " ngày")
                    .build();
        }

        BigDecimal orderAmount = request.getOrderAmount() != null ? request.getOrderAmount() : BigDecimal.ZERO;
        if (discount.getMinOrderAmount() != null && orderAmount.compareTo(discount.getMinOrderAmount()) < 0) {
            return DiscountResultDto.builder()
                    .valid(false)
                    .message("Chỉ áp dụng cho đơn hàng từ " + discount.getMinOrderAmount() + " VND")
                    .build();
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        if ("PERCENT".equalsIgnoreCase(discount.getDiscountType())) {
            BigDecimal rate = discount.getDiscountValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            discountAmount = orderAmount.multiply(rate).setScale(0, RoundingMode.HALF_UP);
        } else {
            // FIXED_AMOUNT
            discountAmount = discount.getDiscountValue();
        }

        if (discountAmount.compareTo(orderAmount) > 0) {
            discountAmount = orderAmount;
        }

        BigDecimal finalAmount = orderAmount.subtract(discountAmount);

        return DiscountResultDto.builder()
                .valid(true)
                .discountCode(discount.getCode())
                .discountType(discount.getDiscountType())
                .discountValue(discount.getDiscountValue())
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .message("Áp dụng mã giảm giá thành công!")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PriceDiscountDto getDiscountByCode(String code) {
        return discountRepository.findByCodeIgnoreCaseAndActiveTrue(code)
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PriceDiscountDto> list(String scopeType, String scopeRefId, Pageable pageable) {
        if (scopeType != null && !scopeType.isBlank()) {
            if (scopeRefId != null && !scopeRefId.isBlank()) {
                return discountRepository.findByScopeTypeAndScopeRefId(scopeType, scopeRefId, pageable).map(this::toDto);
            }
            return discountRepository.findByScopeType(scopeType, pageable).map(this::toDto);
        }
        return discountRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceDiscountDto> getActiveDiscounts(String scopeType, String scopeRefId) {
        LocalDateTime now = LocalDateTime.now();
        List<PriceDiscount> list = new ArrayList<>();

        if (scopeType != null && !scopeType.isBlank()) {
            if (scopeRefId != null && !scopeRefId.isBlank()) {
                list.addAll(discountRepository.findByScopeTypeAndScopeRefIdAndActiveTrue(scopeType, scopeRefId));
            }
            list.addAll(discountRepository.findByScopeTypeAndActiveTrue(scopeType));
        }
        // Always include global active discounts
        list.addAll(discountRepository.findByScopeTypeAndActiveTrue("GLOBAL"));

        return list.stream()
                .filter(d -> d.isActive()
                        && (d.getValidFrom() == null || !now.isBefore(d.getValidFrom()))
                        && (d.getValidTo() == null || !now.isAfter(d.getValidTo()))
                        && (d.getMaxUsage() == null || d.getUsageCount() == null || d.getUsageCount() < d.getMaxUsage()))
                .distinct()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PriceDiscountDto get(Long id) {
        return discountRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    public PriceDiscountDto create(PriceDiscountDto dto) {
        PriceDiscount entity = toEntity(dto);
        if (entity.getValidFrom() == null) {
            entity.setValidFrom(LocalDateTime.now());
        }
        PriceDiscount saved = discountRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public PriceDiscountDto update(Long id, PriceDiscountDto dto) {
        PriceDiscount existing = discountRepository.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPriceListId(dto.getPriceListId());
        existing.setPriceTypeId(dto.getPriceTypeId());
        String scopeType = dto.getScopeType() != null && !dto.getScopeType().isBlank() ? dto.getScopeType() : "GLOBAL";
        String scopeRefId = dto.getScopeRefId();
        if ("GLOBAL".equalsIgnoreCase(scopeType) || (scopeRefId != null && scopeRefId.isBlank())) {
            scopeRefId = null;
        }
        existing.setScopeType(scopeType);
        existing.setScopeRefId(scopeRefId);
        existing.setCode(dto.getCode());
        existing.setDiscountType(dto.getDiscountType());
        existing.setDiscountValue(dto.getDiscountValue());
        existing.setMinDays(dto.getMinDays());
        existing.setMinOrderAmount(dto.getMinOrderAmount());
        existing.setCustomerSegment(dto.getCustomerSegment());
        existing.setValidFrom(dto.getValidFrom());
        existing.setValidTo(dto.getValidTo());
        existing.setMaxUsage(dto.getMaxUsage());
        if (dto.getActive() != null) {
            existing.setActive(dto.getActive());
        } else if (dto.getStatus() != null) {
            existing.setActive("ACTIVE".equalsIgnoreCase(dto.getStatus()));
        }

        PriceDiscount saved = discountRepository.save(existing);
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        discountRepository.deleteById(id);
    }

    private PriceDiscountDto toDto(PriceDiscount e) {
        return PriceDiscountDto.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .priceListId(e.getPriceListId())
                .priceTypeId(e.getPriceTypeId())
                .scopeType(e.getScopeType())
                .scopeRefId(e.getScopeRefId())
                .code(e.getCode())
                .discountType(e.getDiscountType())
                .discountValue(e.getDiscountValue())
                .minDays(e.getMinDays())
                .minOrderAmount(e.getMinOrderAmount())
                .customerSegment(e.getCustomerSegment())
                .validFrom(e.getValidFrom())
                .validTo(e.getValidTo())
                .maxUsage(e.getMaxUsage())
                .usageCount(e.getUsageCount())
                .active(e.isActive())
                .status(e.isActive() ? "ACTIVE" : "INACTIVE")
                .build();
    }

    private PriceDiscount toEntity(PriceDiscountDto d) {
        boolean active = d.getActive() != null ? d.getActive() : !"INACTIVE".equalsIgnoreCase(d.getStatus());
        String scopeType = d.getScopeType() != null && !d.getScopeType().isBlank() ? d.getScopeType() : "GLOBAL";
        String scopeRefId = d.getScopeRefId();
        if ("GLOBAL".equalsIgnoreCase(scopeType) || (scopeRefId != null && scopeRefId.isBlank())) {
            scopeRefId = null;
        }
        return PriceDiscount.builder()
                .name(d.getName())
                .description(d.getDescription())
                .priceListId(d.getPriceListId())
                .priceTypeId(d.getPriceTypeId())
                .scopeType(scopeType)
                .scopeRefId(scopeRefId)
                .code(d.getCode() != null ? d.getCode().trim().toUpperCase() : null)
                .discountType(d.getDiscountType() != null ? d.getDiscountType() : "PERCENT")
                .discountValue(d.getDiscountValue() != null ? d.getDiscountValue() : BigDecimal.ZERO)
                .minDays(d.getMinDays())
                .minOrderAmount(d.getMinOrderAmount())
                .customerSegment(d.getCustomerSegment())
                .validFrom(d.getValidFrom())
                .validTo(d.getValidTo())
                .maxUsage(d.getMaxUsage())
                .usageCount(d.getUsageCount() != null ? d.getUsageCount() : 0)
                .active(active)
                .build();
    }
}
