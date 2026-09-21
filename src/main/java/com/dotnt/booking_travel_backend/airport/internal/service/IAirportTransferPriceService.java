package com.dotnt.booking_travel_backend.airport.internal.service;

import com.dotnt.booking_travel_backend.airport.api.dtos.AirportTransferPriceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAirportTransferPriceService {

    AirportTransferPriceDto create(AirportTransferPriceDto dto);

    Page<AirportTransferPriceDto> list(Pageable pageable);

    AirportTransferPriceDto get(Long id);

    AirportTransferPriceDto update(Long id, AirportTransferPriceDto dto);

    void delete(Long id);

    List<AirportTransferPriceDto> listByRoute(Long routeId);
}