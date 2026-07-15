package com.dotnt.booking_travel_backend.rental.internal.mapper;

import com.dotnt.booking_travel_backend.rental.api.dtos.RentalDto;
import com.dotnt.booking_travel_backend.rental.api.dtos.RentalTypeDto;
import com.dotnt.booking_travel_backend.rental.api.dtos.RentalTypeTranslationDto;
import com.dotnt.booking_travel_backend.rental.internal.entity.Rental;
import com.dotnt.booking_travel_backend.rental.internal.entity.RentalType;
import com.dotnt.booking_travel_backend.rental.internal.entity.RentalTypeTranslation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RentalMapper {
    RentalDto toDto(Rental rental);

    RentalTypeDto toDto(RentalType rentalType);

    RentalTypeTranslationDto toDto(RentalTypeTranslation translation);
}

