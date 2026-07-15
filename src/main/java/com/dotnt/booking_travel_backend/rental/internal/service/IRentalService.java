package com.dotnt.booking_travel_backend.rental.internal.service;

import com.dotnt.booking_travel_backend.rental.api.dtos.RentalDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRentalService {
    RentalDto create(RentalDto dto);
    Page<RentalDto> list(Pageable pageable);
    RentalDto get(Long id);
    RentalDto update(Long id, RentalDto dto);
    void delete(Long id);
}

