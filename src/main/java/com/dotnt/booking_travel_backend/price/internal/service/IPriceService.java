package com.dotnt.booking_travel_backend.price.internal.service;

import com.dotnt.booking_travel_backend.price.api.dtos.PriceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPriceService {
    PriceDto create(PriceDto dto);
    Page<PriceDto> list(Pageable pageable, String businessType, Long businessId);
    PriceDto get(Long id);
    PriceDto update(Long id, PriceDto dto);
    void delete(Long id);
}

