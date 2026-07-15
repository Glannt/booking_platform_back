package com.dotnt.booking_travel_backend.airport.internal.service;

import com.dotnt.booking_travel_backend.airport.api.dtos.AirportRouteDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAirportRouteService {
    AirportRouteDto create(AirportRouteDto dto);
    Page<AirportRouteDto> list(Pageable pageable);
    List<AirportRouteDto> listActiveByAirport(Long airportId);
    AirportRouteDto get(Long id);
    AirportRouteDto update(Long id, AirportRouteDto dto);
    void delete(Long id);
}

