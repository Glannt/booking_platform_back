package com.dotnt.booking_travel_backend.rental.internal.service;

import com.dotnt.booking_travel_backend.rental.api.dtos.RentalTypeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRentalTypeService {
    RentalTypeDto create(RentalTypeDto dto);
    Page<RentalTypeDto> list(Pageable pageable);
    RentalTypeDto get(Long id);
    RentalTypeDto update(Long id, RentalTypeDto dto);
    void delete(Long id);
}

