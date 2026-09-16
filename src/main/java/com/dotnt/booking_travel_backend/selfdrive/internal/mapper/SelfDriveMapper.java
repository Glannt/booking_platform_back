package com.dotnt.booking_travel_backend.selfdrive.internal.mapper;

import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDriveRentalDto;
import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDriveTypeDto;
import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDriveTypeTranslationDto;
import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveRental;
import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveType;
import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveTypeTranslation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SelfDriveMapper {
    SelfDriveRentalDto toDto(SelfDriveRental rental);

    SelfDriveTypeDto toDto(SelfDriveType rentalType);

    SelfDriveTypeTranslationDto toDto(SelfDriveTypeTranslation translation);
}
