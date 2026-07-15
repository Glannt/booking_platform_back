package com.dotnt.booking_travel_backend.fleet.internal.service.impl;

import com.dotnt.booking_travel_backend.fleet.api.dtos.CreateVehicleRequest;
import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleDTO;
import com.dotnt.booking_travel_backend.fleet.internal.entity.Vehicle;
import com.dotnt.booking_travel_backend.fleet.internal.mapper.VehicleMapper;
import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleCategoryRepository;
import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleRepository;
import com.dotnt.booking_travel_backend.fleet.internal.service.IVehicleService;
import com.dotnt.booking_travel_backend.media.internal.entity.Image;
import com.dotnt.booking_travel_backend.media.internal.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class VehicleServiceImpl implements IVehicleService {
    private final VehicleRepository vehicleRepository;
    private final VehicleCategoryRepository vehicleCategoryRepository;
    private final VehicleMapper mapper;
    private final ImageRepository imageRepository;

    @Override
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
                    .year(request.year())
                    .fuelType(request.fuelType() != null ? request.fuelType() : "gasoline")
                    .active(request.active() != null ? request.active() : true)
                    .luggage(request.luggage() != null ? request.luggage() : 0)
                    .luggageUnit(request.luggageUnit())
                    .thumbnailId(request.thumbnailId())
                    .category(vehicleCategoryRepository.findById(request.vehicleCategoryId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid vehicle category ID.")))
                    .build();

            // Set images from IDs
            if (request.imageIds() != null && !request.imageIds().isEmpty()) {
                List<Image> images = imageRepository.findAllById(request.imageIds());
                vehicle.setImages(images);
            }

            Vehicle savedVehicle = vehicleRepository.save(vehicle);
            return toDto(savedVehicle);
        } catch (Exception e) {
            log.error("Error creating vehicle: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Page<VehicleDTO> getVehicles(Pageable pageable, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return vehicleRepository.findAll(pageable).map(this::toDto);
        }
        return vehicleRepository
                .findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(keyword, keyword, pageable)
                .map(this::toDto);
    }

    @Override
    public VehicleDTO getVehicle(Long id) {
        return vehicleRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    public VehicleDTO updateVehicle(Long id, CreateVehicleRequest request) {
        return vehicleRepository.findById(id).map(existing -> {
            if (request.brand() != null) existing.setBrand(request.brand());
            if (request.model() != null) existing.setModel(request.model());
            if (request.year() != null) existing.setYear(request.year());
            if (request.fuelType() != null) existing.setFuelType(request.fuelType());
            if (request.active() != null) existing.setActive(request.active());
            if (request.luggage() != null) existing.setLuggage(request.luggage());
            if (request.luggageUnit() != null) existing.setLuggageUnit(request.luggageUnit());
            if (request.thumbnailId() != null) existing.setThumbnailId(request.thumbnailId());
            if (request.imageIds() != null) {
                List<Image> images = imageRepository.findAllById(request.imageIds());
                existing.setImages(images);
            }
            if (request.vehicleCategoryId() != null) {
                existing.setCategory(vehicleCategoryRepository.findById(request.vehicleCategoryId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid vehicle category ID.")));
            }
            Vehicle saved = vehicleRepository.save(existing);
            return toDto(saved);
        }).orElse(null);
    }

    @Override
    public void deleteVehicle(Long id) {
        if (vehicleRepository.existsById(id)) {
            vehicleRepository.deleteById(id);
        }
    }

    private VehicleDTO toDto(Vehicle v) {
        String thumbnailUrl = null;
        if (v.getThumbnailId() != null) {
            thumbnailUrl = imageRepository.findById(v.getThumbnailId())
                    .map(img -> img.getFilePath())
                    .orElse(null);
        }
        List<Long> imageIds = v.getImages() != null
                ? v.getImages().stream().map(Image::getId).toList()
                : List.of();
        List<com.dotnt.booking_travel_backend.media.api.MediaDto> images = v.getImages() != null
                ? v.getImages().stream().map(img -> com.dotnt.booking_travel_backend.media.api.MediaDto.builder()
                        .id(img.getId())
                        .fileName(img.getFileName())
                        .filePath(img.getFilePath())
                        .fileSize(img.getFileSize())
                        .mimeType(img.getMimeType())
                        .width(img.getWidth())
                        .height(img.getHeight())
                        .build()).toList()
                : List.of();
        return VehicleDTO.builder()
                .id(v.getId())
                .brand(v.getBrand())
                .model(v.getModel())
                .year(v.getYear())
                .fuelType(v.getFuelType())
                .active(v.isActive())
                .luggage(v.getLuggage())
                .luggageUnit(v.getLuggageUnit())
                .thumbnailId(v.getThumbnailId())
                .thumbnailUrl(thumbnailUrl)
                .imageIds(imageIds)
                .images(images)
                .type(mapper.toCategoryDto(v.getCategory()))
                .build();
    }
}
