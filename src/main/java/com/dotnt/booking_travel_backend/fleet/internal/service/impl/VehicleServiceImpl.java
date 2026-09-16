package com.dotnt.booking_travel_backend.fleet.internal.service.impl;

import com.dotnt.booking_travel_backend.fleet.api.dtos.*;
import com.dotnt.booking_travel_backend.fleet.internal.entity.AddOn;
import com.dotnt.booking_travel_backend.fleet.internal.entity.Amenity;
import com.dotnt.booking_travel_backend.fleet.internal.entity.Vehicle;
import com.dotnt.booking_travel_backend.fleet.internal.mapper.VehicleMapper;
import com.dotnt.booking_travel_backend.fleet.internal.repository.AddOnRepository;
import com.dotnt.booking_travel_backend.fleet.internal.repository.AmenityRepository;
import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleCategoryRepository;
import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleRepository;
import com.dotnt.booking_travel_backend.fleet.internal.service.IAddOnService;
import com.dotnt.booking_travel_backend.fleet.internal.service.IAmenityService;
import com.dotnt.booking_travel_backend.fleet.internal.service.IVehicleService;
import com.dotnt.booking_travel_backend.fleet.internal.strategy.VehicleMatchingContext;
import com.dotnt.booking_travel_backend.media.api.MediaDto;
import com.dotnt.booking_travel_backend.shared.module.ModuleEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class VehicleServiceImpl implements IVehicleService {
    private final VehicleRepository vehicleRepository;
    private final VehicleCategoryRepository vehicleCategoryRepository;
    private final VehicleMapper mapper;
    private final ModuleEventPublisher moduleEventPublisher;
    private final AddOnRepository addOnRepository;
    private final AmenityRepository amenityRepository;
    private final VehicleMatchingContext vehicleMatchingContext;

    @Override
    @Transactional
    @CacheEvict(value = "vehicles", allEntries = true)
    public VehicleDTO createVehicle(CreateVehicleRequest request) {
        try {
            if (request.brand() == null
                    || request.model() == null
                    || request.year() == null
                    || request.vehicleCategoryId() == null) {
                throw new IllegalArgumentException("Brand, model, year, and vehicle category ID are required fields.");
            }
            Vehicle vehicle = Vehicle.builder()
                    .brand(request.brand())
                    .model(request.model())
                    .licensePlate(request.licensePlate())
                    .year(request.year())
                    .fuelType(request.fuelType() != null ? request.fuelType() : "gasoline")
                    .active(request.active() != null ? request.active() : true)
                    .canRent(request.canRent() != null ? request.canRent() : true)
                    .luggage(request.luggage() != null ? request.luggage() : 0)
                    .luggageUnit(request.luggageUnit())
                    .thumbnailId(request.thumbnailId())
                    .category(vehicleCategoryRepository.findById(request.vehicleCategoryId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid vehicle category ID.")))
                    .build();

            // Set images from IDs
            if (request.imageIds() != null && !request.imageIds().isEmpty()) {
                vehicle.setImageIds(new ArrayList<>(request.imageIds()));
            }

            // Set add-ons from IDs
            if (request.addOnIds() != null && !request.addOnIds().isEmpty()) {
                List<AddOn> addOns = addOnRepository.findAllById(request.addOnIds());
                vehicle.setAddOns(addOns);
            }

            // Set amenities from IDs
            if (request.amenityIds() != null && !request.amenityIds().isEmpty()) {
                List<Amenity> amenities = amenityRepository.findAllById(request.amenityIds());
                vehicle.setAmenities(amenities);
            }

            Vehicle savedVehicle = vehicleRepository.save(vehicle);
            return toDto(savedVehicle);
        } catch (Exception e) {
            log.error("Error creating vehicle: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "vehicles", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #keyword")
    public Page<VehicleDTO> getVehicles(Pageable pageable, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return vehicleRepository.findAll(pageable).map(this::toDto);
        }
        return vehicleRepository
                .findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(keyword, keyword, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "vehicles", key = "#id")
    public VehicleDTO getVehicle(Long id) {
        return vehicleRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "vehicles", allEntries = true)
    public VehicleDTO updateVehicle(Long id, CreateVehicleRequest request) {
        return vehicleRepository.findById(id).map(existing -> {
            if (request.brand() != null) existing.setBrand(request.brand());
            if (request.model() != null) existing.setModel(request.model());
            if (request.licensePlate() != null) existing.setLicensePlate(request.licensePlate());
            if (request.year() != null) existing.setYear(request.year());
            if (request.fuelType() != null) existing.setFuelType(request.fuelType());
            if (request.active() != null) existing.setActive(request.active());
            if (request.canRent() != null) existing.setCanRent(request.canRent());
            if (request.luggage() != null) existing.setLuggage(request.luggage());
            if (request.luggageUnit() != null) existing.setLuggageUnit(request.luggageUnit());
            if (request.thumbnailId() != null) existing.setThumbnailId(request.thumbnailId());
            if (request.imageIds() != null) {
                existing.setImageIds(new ArrayList<>(request.imageIds()));
            }
            if (request.vehicleCategoryId() != null) {
                existing.setCategory(vehicleCategoryRepository.findById(request.vehicleCategoryId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid vehicle category ID.")));
            }
            if (request.addOnIds() != null) {
                List<AddOn> addOns = addOnRepository.findAllById(request.addOnIds());
                existing.setAddOns(addOns);
            }
            if (request.amenityIds() != null) {
                List<Amenity> amenities = amenityRepository.findAllById(request.amenityIds());
                existing.setAmenities(amenities);
            }

            Vehicle saved = vehicleRepository.save(existing);
            return toDto(saved);
        }).orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "vehicles", allEntries = true)
    public void deleteVehicle(Long id) {
        if (vehicleRepository.existsById(id)) {
            vehicleRepository.deleteById(id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "vehicles", key = "'rentable_' + #categoryId")
    public List<VehicleDTO> getRentableVehiclesByCategory(Long categoryId) {
        return vehicleRepository.findByCategoryIdAndCanRentTrueAndActiveTrue(categoryId)
                .stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleDTO> matchVehicles(VehicleMatchingCriteria criteria) {
        List<Vehicle> matched = vehicleMatchingContext.executeMatching(criteria);
        return matched.stream().map(this::toDto).toList();
    }

    @SuppressWarnings("unchecked")
    private VehicleDTO toDto(Vehicle v) {
        String thumbnailUrl = null;
        if (v.getThumbnailId() != null) {
            try {
                thumbnailUrl = moduleEventPublisher.publishRequest(
                        "media.image.resolveThumbnailUrl", v.getThumbnailId(), String.class);
            } catch (Exception e) {
                log.debug("Could not resolve thumbnail url for vehicle {}: {}", v.getId(), e.getMessage());
            }
        }
        List<Long> imageIds = v.getImageIds() != null ? v.getImageIds() : List.of();
        List<MediaDto> images = List.of();
        if (!imageIds.isEmpty()) {
            try {
                List<MediaDto> fetched = moduleEventPublisher.publishRequest(
                        "media.image.findAllByIds", imageIds, List.class);
                if (fetched != null) images = fetched;
            } catch (Exception e) {
                log.warn("Failed to fetch media for vehicle {}: {}", v.getId(), e.getMessage());
            }
        }

        List<AddOnDTO> addOnDTOs = v.getAddOns() != null
                ? v.getAddOns().stream().map(ao -> {
                    List<AddOnTranslationDTO> trans = ao.getTranslations() != null
                            ? ao.getTranslations().stream().map(t -> AddOnTranslationDTO.builder()
                                    .id(t.getId())
                                    .locale(t.getLocale())
                                    .name(t.getName())
                                    .description(t.getDescription())
                                    .build()).toList()
                            : List.of();
                    List<AddOnCategoryTranslationDTO> catTrans = ao.getCategory() != null && ao.getCategory().getTranslations() != null
                            ? ao.getCategory().getTranslations().stream().map(t -> AddOnCategoryTranslationDTO.builder()
                                    .id(t.getId())
                                    .locale(t.getLocale())
                                    .name(t.getName())
                                    .description(t.getDescription())
                                    .build()).toList()
                            : List.of();
                    String catName = null;
                    if (ao.getCategory() != null && ao.getCategory().getTranslations() != null && !ao.getCategory().getTranslations().isEmpty()) {
                        catName = ao.getCategory().getTranslations().get(0).getName();
                    }
                    return AddOnDTO.builder()
                            .id(ao.getId())
                            .categoryId(ao.getCategory() != null ? ao.getCategory().getId() : null)
                            .categoryCode(ao.getCategory() != null ? ao.getCategory().getCode() : null)
                            .categoryName(catName)
                            .categoryTranslations(catTrans)
                            .code(ao.getCode())
                            .value(ao.getValue())
                            .icon(ao.getIcon())
                            .displayOrder(ao.getDisplayOrder())
                            .active(ao.isActive())
                            .translations(trans)
                            .build();
                }).toList()
                : List.of();

        List<AmenityDTO> amenityDTOs = v.getAmenities() != null
                ? v.getAmenities().stream().map(am -> {
                    List<AmenityTranslationDTO> trans = am.getTranslations() != null
                            ? am.getTranslations().stream().map(t -> AmenityTranslationDTO.builder()
                                    .id(t.getId())
                                    .locale(t.getLocale())
                                    .name(t.getName())
                                    .description(t.getDescription())
                                    .build()).toList()
                            : List.of();
                    List<AmenityCategoryTranslationDTO> catTrans = am.getCategory() != null && am.getCategory().getTranslations() != null
                            ? am.getCategory().getTranslations().stream().map(t -> AmenityCategoryTranslationDTO.builder()
                                    .id(t.getId())
                                    .locale(t.getLocale())
                                    .name(t.getName())
                                    .description(t.getDescription())
                                    .build()).toList()
                            : List.of();
                    String catName = null;
                    if (am.getCategory() != null && am.getCategory().getTranslations() != null && !am.getCategory().getTranslations().isEmpty()) {
                        catName = am.getCategory().getTranslations().get(0).getName();
                    }
                    return AmenityDTO.builder()
                            .id(am.getId())
                            .categoryId(am.getCategory() != null ? am.getCategory().getId() : null)
                            .categoryCode(am.getCategory() != null ? am.getCategory().getCode() : null)
                            .categoryName(catName)
                            .categoryTranslations(catTrans)
                            .code(am.getCode())
                            .icon(am.getIcon())
                            .displayOrder(am.getDisplayOrder())
                            .active(am.isActive())
                            .translations(trans)
                            .build();
                }).toList()
                : List.of();

        return VehicleDTO.builder()
                .id(v.getId())
                .brand(v.getBrand())
                .model(v.getModel())
                .licensePlate(v.getLicensePlate())
                .year(v.getYear())
                .fuelType(v.getFuelType())
                .active(v.isActive())
                .canRent(v.isCanRent())
                .luggage(v.getLuggage())
                .luggageUnit(v.getLuggageUnit())
                .thumbnailId(v.getThumbnailId())
                .thumbnailUrl(thumbnailUrl)
                .imageIds(imageIds)
                .images(images)
                .type(mapper.toCategoryDto(v.getCategory()))
                .addOns(addOnDTOs)
                .amenities(amenityDTOs)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MediaDto> getCategoryImages(Long categoryId) {
        List<Vehicle> vehicles = vehicleRepository.findByCategoryIdAndCanRentTrueAndActiveTrue(categoryId);
        java.util.Set<Long> seenIds = new java.util.LinkedHashSet<>();

        for (Vehicle v : vehicles) {
            if (v.getThumbnailId() != null) {
                seenIds.add(v.getThumbnailId());
            }
            if (v.getImageIds() != null) {
                seenIds.addAll(v.getImageIds());
            }
        }
        if (seenIds.isEmpty()) {
            return java.util.List.of();
        }
        try {
            List<MediaDto> dtos = moduleEventPublisher.publishRequest(
                    "media.image.findAllByIds", new ArrayList<>(seenIds), List.class);
            return dtos != null ? dtos : java.util.List.of();
        } catch (Exception e) {
            log.warn("Failed to fetch category images for category {}: {}", categoryId, e.getMessage());
            return java.util.List.of();
        }
    }
}
