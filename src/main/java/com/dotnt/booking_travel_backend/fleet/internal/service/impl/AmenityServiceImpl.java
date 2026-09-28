package com.dotnt.booking_travel_backend.fleet.internal.service.impl;

import com.dotnt.booking_travel_backend.fleet.api.dtos.*;
import com.dotnt.booking_travel_backend.fleet.internal.entity.*;
import com.dotnt.booking_travel_backend.fleet.internal.repository.*;
import com.dotnt.booking_travel_backend.fleet.internal.service.IAmenityService;
import com.dotnt.booking_travel_backend.shared.utils.EntityMergeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AmenityServiceImpl implements IAmenityService {

    private final AmenityCategoryRepository categoryRepository;
    private final AmenityRepository amenityRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public AmenityCategoryDTO createCategory(CreateAmenityCategoryRequest request) {
        AmenityCategory category = AmenityCategory.builder()
                .code(request.code().trim().toUpperCase())
                .icon(request.icon())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .active(request.active() != null ? request.active() : true)
                .build();

        if (request.translations() != null) {
            for (AmenityCategoryTranslationDTO tDto : request.translations()) {
                AmenityCategoryTranslation translation = AmenityCategoryTranslation.builder()
                        .amenityCategory(category)
                        .locale(tDto.getLocale().toLowerCase())
                        .name(tDto.getName())
                        .description(tDto.getDescription())
                        .build();
                category.addTranslation(translation);
            }
        }

        AmenityCategory saved = categoryRepository.save(category);
        return toCategoryDTO(saved);
    }

    @Override
    @Transactional
    public AmenityCategoryDTO updateCategory(Long id, CreateAmenityCategoryRequest request) {
        AmenityCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AmenityCategory not found with id: " + id));

        if (request.code() != null) category.setCode(request.code().trim().toUpperCase());
        if (request.icon() != null) category.setIcon(request.icon());
        if (request.displayOrder() != null) category.setDisplayOrder(request.displayOrder());
        if (request.active() != null) category.setActive(request.active());

        if (request.translations() != null) {
            final AmenityCategory finalCategory = category;
            EntityMergeUtils.mergeCollection(
                    category.getTranslations(),
                    request.translations(),
                    t -> t.getLocale() != null ? t.getLocale().toLowerCase() : null,
                    tDto -> tDto.getLocale() != null ? tDto.getLocale().toLowerCase() : null,
                    (existing, tDto) -> {
                        existing.setName(tDto.getName());
                        existing.setDescription(tDto.getDescription());
                    },
                    tDto -> AmenityCategoryTranslation.builder()
                            .amenityCategory(finalCategory)
                            .locale(tDto.getLocale().toLowerCase())
                            .name(tDto.getName())
                            .description(tDto.getDescription())
                            .build()
            );
        }

        AmenityCategory updated = categoryRepository.save(category);
        return toCategoryDTO(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        AmenityCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AmenityCategory not found with id: " + id));
        // Detach every item from vehicles first (vehicle_amenities join table), then cascade-delete the category
        for (Amenity amenity : new ArrayList<>(category.getAmenities())) {
            for (Vehicle vehicle : vehicleRepository.findAllByAmenities_Id(amenity.getId())) {
                vehicle.removeAmenity(amenity);
            }
        }
        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public AmenityCategoryDTO getCategoryById(Long id) {
        return categoryRepository.findById(id).map(this::toCategoryDTO).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmenityCategoryDTO> getAllCategories(Boolean activeOnly) {
        List<AmenityCategory> list = (activeOnly != null && activeOnly)
                ? categoryRepository.findByActiveTrueOrderByDisplayOrderAsc()
                : categoryRepository.findAll();
        return list.stream().map(this::toCategoryDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AmenityCategoryDTO> getCategoriesPaged(String keyword, Pageable pageable) {
        Page<AmenityCategory> page = (keyword != null && !keyword.isBlank())
                ? categoryRepository.findByCodeContainingIgnoreCase(keyword.trim(), pageable)
                : categoryRepository.findAll(pageable);
        return page.map(this::toCategoryDTO);
    }

    @Override
    @Transactional
    public AmenityDTO createAmenity(CreateAmenityRequest request) {
        AmenityCategory category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.categoryId()));
        } else if (request.categoryCode() != null) {
            category = categoryRepository.findByCode(request.categoryCode().trim().toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Category not found with code: " + request.categoryCode()));
        } else {
            throw new IllegalArgumentException("Category id or code must be provided");
        }

        Amenity amenity = Amenity.builder()
                .category(category)
                .code(request.code().trim().toUpperCase())
                .icon(request.icon())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .active(request.active() != null ? request.active() : true)
                .build();

        if (request.translations() != null) {
            for (AmenityTranslationDTO tDto : request.translations()) {
                AmenityTranslation translation = AmenityTranslation.builder()
                        .amenity(amenity)
                        .locale(tDto.getLocale().toLowerCase())
                        .name(tDto.getName())
                        .description(tDto.getDescription())
                        .build();
                amenity.addTranslation(translation);
            }
        }

        Amenity saved = amenityRepository.save(amenity);
        return toAmenityDTO(saved);
    }

    @Override
    @Transactional
    public AmenityDTO updateAmenity(Long id, CreateAmenityRequest request) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Amenity not found with id: " + id));

        if (request.categoryId() != null && !request.categoryId().equals(amenity.getCategory().getId())) {
            AmenityCategory category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.categoryId()));
            amenity.setCategory(category);
        }
        if (request.code() != null) amenity.setCode(request.code().trim().toUpperCase());
        if (request.icon() != null) amenity.setIcon(request.icon());
        if (request.displayOrder() != null) amenity.setDisplayOrder(request.displayOrder());
        if (request.active() != null) amenity.setActive(request.active());

        if (request.translations() != null) {
            final Amenity finalAmenity = amenity;
            EntityMergeUtils.mergeCollection(
                    amenity.getTranslations(),
                    request.translations(),
                    t -> t.getLocale() != null ? t.getLocale().toLowerCase() : null,
                    tDto -> tDto.getLocale() != null ? tDto.getLocale().toLowerCase() : null,
                    (existing, tDto) -> {
                        existing.setName(tDto.getName());
                        existing.setDescription(tDto.getDescription());
                    },
                    tDto -> AmenityTranslation.builder()
                            .amenity(finalAmenity)
                            .locale(tDto.getLocale().toLowerCase())
                            .name(tDto.getName())
                            .description(tDto.getDescription())
                            .build()
            );
        }

        Amenity updated = amenityRepository.save(amenity);
        return toAmenityDTO(updated);
    }

    @Override
    @Transactional
    public void deleteAmenity(Long id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Amenity not found with id: " + id));
        // Detach from all vehicles first (vehicle_amenities join table), then delete the item
        for (Vehicle vehicle : vehicleRepository.findAllByAmenities_Id(id)) {
            vehicle.removeAmenity(amenity);
        }
        amenityRepository.delete(amenity);
    }

    @Override
    @Transactional(readOnly = true)
    public AmenityDTO getAmenityById(Long id) {
        return amenityRepository.findById(id).map(this::toAmenityDTO).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmenityDTO> getAmenitiesByCategoryId(Long categoryId) {
        return amenityRepository.findByCategoryId(categoryId).stream()
                .map(this::toAmenityDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmenityDTO> getAmenitiesByCategoryCode(String categoryCode) {
        return amenityRepository.findByCategoryCode(categoryCode).stream()
                .map(this::toAmenityDTO)
                .collect(Collectors.toList());
    }

    // Public localized API implementations
    @Override
    @Transactional(readOnly = true)
    public List<PublicAmenityCategoryDTO> getPublicCategories(String locale) {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc().stream().map(cat -> {
            AmenityCategoryTranslation trans = cat.getTranslation(locale);
            String name = trans != null ? trans.getName() : cat.getCode();
            String desc = trans != null ? trans.getDescription() : null;

            List<PublicAmenityDTO> publicAmenities = cat.getAmenities().stream()
                    .filter(Amenity::isActive)
                    .map(am -> {
                        AmenityTranslation amTrans = am.getTranslation(locale);
                        return PublicAmenityDTO.builder()
                                .id(am.getId())
                                .categoryCode(cat.getCode())
                                .code(am.getCode())
                                .name(amTrans != null ? amTrans.getName() : am.getCode())
                                .description(amTrans != null ? amTrans.getDescription() : null)
                                .icon(am.getIcon() != null ? am.getIcon() : cat.getIcon())
                                .displayOrder(am.getDisplayOrder())
                                .build();
                    })
                    .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                    .collect(Collectors.toList());

            return PublicAmenityCategoryDTO.builder()
                    .id(cat.getId())
                    .code(cat.getCode())
                    .name(name)
                    .description(desc)
                    .icon(cat.getIcon())
                    .displayOrder(cat.getDisplayOrder())
                    .amenities(publicAmenities)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicAmenityDTO> getPublicAmenitiesByCategory(String categoryCode, String locale) {
        return amenityRepository.findByCategoryCode(categoryCode).stream()
                .filter(Amenity::isActive)
                .map(am -> {
                    AmenityTranslation trans = am.getTranslation(locale);
                    return PublicAmenityDTO.builder()
                            .id(am.getId())
                            .categoryCode(am.getCategory().getCode())
                            .code(am.getCode())
                            .name(trans != null ? trans.getName() : am.getCode())
                            .description(trans != null ? trans.getDescription() : null)
                            .icon(am.getIcon() != null ? am.getIcon() : am.getCategory().getIcon())
                            .displayOrder(am.getDisplayOrder())
                            .build();
                }).collect(Collectors.toList());
    }

    // Helper mappers
    public AmenityCategoryDTO toCategoryDTO(AmenityCategory category) {
        if (category == null) return null;
        List<AmenityCategoryTranslationDTO> tDtos = category.getTranslations() != null
                ? category.getTranslations().stream().map(t -> AmenityCategoryTranslationDTO.builder()
                        .id(t.getId())
                        .locale(t.getLocale())
                        .name(t.getName())
                        .description(t.getDescription())
                        .build()).collect(Collectors.toList())
                : new ArrayList<>();

        List<AmenityDTO> amenityDtos = category.getAmenities() != null
                ? category.getAmenities().stream().map(this::toAmenityDTO).collect(Collectors.toList())
                : new ArrayList<>();

        return AmenityCategoryDTO.builder()
                .id(category.getId())
                .code(category.getCode())
                .icon(category.getIcon())
                .displayOrder(category.getDisplayOrder())
                .active(category.isActive())
                .translations(tDtos)
                .amenities(amenityDtos)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .createdBy(category.getCreatedBy())
                .build();
    }

    public AmenityDTO toAmenityDTO(Amenity amenity) {
        if (amenity == null) return null;
        List<AmenityTranslationDTO> tDtos = amenity.getTranslations() != null
                ? amenity.getTranslations().stream().map(t -> AmenityTranslationDTO.builder()
                        .id(t.getId())
                        .locale(t.getLocale())
                        .name(t.getName())
                        .description(t.getDescription())
                        .build()).collect(Collectors.toList())
                : new ArrayList<>();

        return AmenityDTO.builder()
                .id(amenity.getId())
                .categoryId(amenity.getCategory() != null ? amenity.getCategory().getId() : null)
                .categoryCode(amenity.getCategory() != null ? amenity.getCategory().getCode() : null)
                .code(amenity.getCode())
                .icon(amenity.getIcon())
                .displayOrder(amenity.getDisplayOrder())
                .active(amenity.isActive())
                .translations(tDtos)
                .createdAt(amenity.getCreatedAt())
                .updatedAt(amenity.getUpdatedAt())
                .createdBy(amenity.getCreatedBy())
                .build();
    }
}
