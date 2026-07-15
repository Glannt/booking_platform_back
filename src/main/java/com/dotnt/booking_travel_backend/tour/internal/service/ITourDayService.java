package com.dotnt.booking_travel_backend.tour.internal.service;

import com.dotnt.booking_travel_backend.tour.api.dtos.TourDayDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITourDayService {
    TourDayDto create(Long tourId, TourDayDto dto);
    Page<TourDayDto> list(Long tourId, Pageable pageable);
    TourDayDto get(Long id);
    TourDayDto update(Long id, TourDayDto dto);
    void delete(Long id);
}

