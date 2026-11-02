package com.dotnt.booking_travel_backend.tour.internal.service;

import com.dotnt.booking_travel_backend.tour.api.dtos.TourActivityDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITourActivityService {
    TourActivityDto create(Long tourDayId, TourActivityDto dto);
    Page<TourActivityDto> list(Long tourDayId, Pageable pageable);
    TourActivityDto get(Long id);
    TourActivityDto update(Long id, TourActivityDto dto);
    void delete(Long id);
}

