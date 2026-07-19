package com.dotnt.booking_travel_backend.rental.internal.service;

import com.dotnt.booking_travel_backend.rental.api.dtos.IntercityDepartureTimeDto;

import java.util.List;

public interface IIntercityDepartureTimeService {
    List<IntercityDepartureTimeDto> listByRoute(Long routeId);
    List<IntercityDepartureTimeDto> listActiveByRoute(Long routeId);
    IntercityDepartureTimeDto get(Long id);
    IntercityDepartureTimeDto create(IntercityDepartureTimeDto dto);
    IntercityDepartureTimeDto update(Long id, IntercityDepartureTimeDto dto);
    void delete(Long id);
}
