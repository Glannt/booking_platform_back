package com.dotnt.booking_travel_backend.discount.api;

import com.dotnt.booking_travel_backend.discount.api.dtos.DiscountResultDto;
import com.dotnt.booking_travel_backend.discount.api.dtos.PriceDiscountDto;
import com.dotnt.booking_travel_backend.discount.api.dtos.ValidateDiscountRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiscountService {
    DiscountResultDto validateAndApplyDiscount(ValidateDiscountRequest request);
    PriceDiscountDto getDiscountByCode(String code);
    Page<PriceDiscountDto> list(String scopeType, String scopeRefId, Pageable pageable);
    List<PriceDiscountDto> getActiveDiscounts(String scopeType, String scopeRefId);
    PriceDiscountDto get(Long id);
    PriceDiscountDto create(PriceDiscountDto dto);
    PriceDiscountDto update(Long id, PriceDiscountDto dto);
    void delete(Long id);
}
