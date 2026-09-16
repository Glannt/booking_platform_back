package com.dotnt.booking_travel_backend.tour.internal.service;

import com.dotnt.booking_travel_backend.tour.api.dtos.TourConfigDto;

public interface TourConfigService {
    TourConfigDto getConfig();
    TourConfigDto updateConfig(TourConfigDto dto);
}
