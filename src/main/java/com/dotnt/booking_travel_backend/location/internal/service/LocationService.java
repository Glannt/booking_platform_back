package com.dotnt.booking_travel_backend.location.internal.service;

import com.dotnt.booking_travel_backend.location.api.dtos.LocationDto;
import com.dotnt.booking_travel_backend.location.api.dtos.LocationSearchResultDto;

import java.util.List;

public interface LocationService {
    List<LocationSearchResultDto> search(String query, String locale, int limit);
    List<LocationDto> listActive(String locale);
    LocationDto get(Long id, String locale);
}
