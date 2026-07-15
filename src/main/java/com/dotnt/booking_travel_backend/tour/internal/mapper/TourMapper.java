package com.dotnt.booking_travel_backend.tour.internal.mapper;

import com.dotnt.booking_travel_backend.tour.api.dtos.*;
import com.dotnt.booking_travel_backend.tour.internal.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TourMapper {
    TourMapper INSTANCE = Mappers.getMapper(TourMapper.class);

    @Mapping(target = "days", ignore = true)
    TourDto toDto(Tour tour);

    TourDayDto toDto(TourDay day);

    TourActivityDto toDto(TourDayActivity activity);

    TourDayTranslationDto toDto(TourDayTranslation translation);

    TourActivityTranslationDto toDto(TourDayActivityTranslation translation);
}
