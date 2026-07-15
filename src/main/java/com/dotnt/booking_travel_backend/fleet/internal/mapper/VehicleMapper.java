package com.dotnt.booking_travel_backend.fleet.internal.mapper;

import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleCategoryDTO;
import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleCategoryTranslationDTO;
import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleDTO;
import com.dotnt.booking_travel_backend.fleet.internal.entity.Vehicle;
import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategory;
import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategoryTranslation;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper
public interface VehicleMapper {
    @Mapping(source = "category", target = "type")
    VehicleDTO toDto(Vehicle vehicle);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDtoFromSource(Vehicle source, @MappingTarget VehicleDTO target);

    VehicleCategoryDTO toCategoryDto(VehicleCategory source);

    List<VehicleCategoryTranslationDTO> toTranslationDtoList(List<VehicleCategoryTranslation> sources);

    VehicleCategoryTranslationDTO toTranslationDto(VehicleCategoryTranslation source);
}
