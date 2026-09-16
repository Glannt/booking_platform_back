package com.dotnt.booking_travel_backend.promotion.api;

import com.dotnt.booking_travel_backend.promotion.api.dtos.PromotionDto;
import com.dotnt.booking_travel_backend.promotion.api.dtos.PromotionGrantDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PromotionService {

    Page<PromotionDto> list(String scopeType, String scopeRefId, String issuanceType, Pageable pageable);

    PromotionDto get(Long id);

    PromotionDto getByCode(String code);

    PromotionDto create(PromotionDto dto);

    PromotionDto update(Long id, PromotionDto dto);

    void delete(Long id);

    List<PromotionDto> getActivePromotions(String scopeType, String scopeRefId);

    List<PromotionGrantDto> getCustomerAvailableGrants(Long customerId);

    PromotionGrantDto grantPromotion(Long promotionId, Long customerId, String reason, Integer expireDays, String customCode);

    List<PromotionGrantDto> getGrantsByPromotionId(Long promotionId);

    List<PromotionGrantDto> getGrantsByCustomerId(Long customerId);
}
