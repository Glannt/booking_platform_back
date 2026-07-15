package com.dotnt.booking_travel_backend.airport.internal.mapper;

import com.dotnt.booking_travel_backend.airport.api.dtos.AirportTransferDto;
import com.dotnt.booking_travel_backend.airport.api.dtos.AirportRouteDto;
import com.dotnt.booking_travel_backend.airport.internal.entity.AirportTransfer;
import com.dotnt.booking_travel_backend.airport.internal.entity.AirportRoute;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AirportTransferMapper {
    AirportTransferDto toDto(AirportTransfer t);

    AirportRouteDto toDto(AirportRoute r);
}

