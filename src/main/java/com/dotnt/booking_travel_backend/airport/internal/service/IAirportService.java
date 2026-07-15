package com.dotnt.booking_travel_backend.airport.internal.service;

import com.dotnt.booking_travel_backend.airport.api.dtos.AirportDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAirportService {
    AirportDto create(AirportDto dto);
    Page<AirportDto> list(Pageable pageable);
    List<AirportDto> listActive();
    AirportDto get(Long id);
    AirportDto update(Long id, AirportDto dto);
    void delete(Long id);
}
