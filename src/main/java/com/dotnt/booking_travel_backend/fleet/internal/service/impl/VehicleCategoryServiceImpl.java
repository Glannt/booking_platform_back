package com.dotnt.booking_travel_backend.fleet.internal.service.impl;

import com.dotnt.booking_travel_backend.fleet.api.dtos.CreateVehicleCategoryRequest;
import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleCategoryDTO;
import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleCategoryTranslationDTO;
import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategory;
import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategoryTranslation;
import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleCategoryRepository;
import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleCategoryTranslationRepository;
import com.dotnt.booking_travel_backend.fleet.internal.service.IVehicleCategoryService;
import com.dotnt.booking_travel_backend.fleet.internal.service.specs.VehicleCategorySpecification;
import com.dotnt.booking_travel_backend.shared.module.ModuleEventPublisher;
import com.dotnt.booking_travel_backend.shared.specs.GenericSpecification;
import com.dotnt.booking_travel_backend.shared.specs.SearchOperation;
import com.dotnt.booking_travel_backend.shared.utils.EntityMergeUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@RequiredArgsConstructor
public class VehicleCategoryServiceImpl implements IVehicleCategoryService {

    private final VehicleCategoryRepository vehicleCategoryRepository;
    private final VehicleCategoryTranslationRepository vehicleCategoryTranslationRepository;
    private final ModuleEventPublisher moduleEventPublisher;
    private final com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleRepository vehicleRepository;

    private Integer resolveTotalVehicles(VehicleCategory cat) {
        if (cat.getTotalVehicles() != null && cat.getTotalVehicles() > 0) {
            return cat.getTotalVehicles();
        }
        if (cat.getId() == null) return 0;
        return vehicleRepository.findByCategoryIdAndCanRentTrueAndActiveTrue(cat.getId()).size();
    }

    private Integer resolveAvailableVehicles(VehicleCategory cat, Integer total) {
        if (cat.getAvailableVehicles() != null) {
            return cat.getAvailableVehicles();
        }
        return total;
    }

    private String resolveThumbnailUrl(Long thumbnailId) {
        if (thumbnailId == null) return null;
        try {
            return moduleEventPublisher.publishRequest(
                    "media.image.resolveThumbnailUrl", thumbnailId, String.class);
        } catch (Exception e) {
            log.debug("Could not resolve thumbnail url for id {}: {}", thumbnailId, e.getMessage());
            return null;
        }
    }
    @Override
    @CacheEvict(value = "vehicleCategories", allEntries = true)
    public VehicleCategoryDTO createVehicleCategory(CreateVehicleCategoryRequest request) {
        if(request.code() == null || request.minSeat() == null || request.maxSeat() == null) {
            log.error("Invalid request data: {}", request);
            throw new IllegalArgumentException("Invalid request data");// fix đa ngôn ngữ sau
        }
        if(request.translations().isEmpty()) {
            log.error("Translations cannot be empty: {}", request);
            throw new IllegalArgumentException("Translations cannot be empty");// fix đa ngôn ngữ sau
        }
        VehicleCategory vehicleCategory = VehicleCategory.builder()
            .code(request.code())
            .minSeat(request.minSeat())
            .maxSeat(request.maxSeat())
            .minLuggage(request.minLuggage())
            .maxLuggage(request.maxLuggage())
            .unitLuggage(request.unitLuggage())
            .active(request.active() != null ? request.active() : true)
            .isDefault(request.isDefault() != null ? request.isDefault() : false)
            .thumbnailId(request.thumbnailId())
            .totalVehicles(request.totalVehicles())
            .availableVehicles(request.availableVehicles())
            .build();
        vehicleCategoryRepository.save(vehicleCategory);

        List<VehicleCategoryTranslation> translations = request.translations().stream()
                .map(t -> VehicleCategoryTranslation.builder()
                        .vehicleCategory(vehicleCategory)
                        .locale(t.getLocale())
                        .name(t.getName())
                        .slug(t.getSlug() != null ? t.getSlug() : (t.getName() != null ? t.getName().toLowerCase().replaceAll("[^a-zA-Z0-9-]", "-") : "category"))
                        .description(t.getDescription())
                        .build())
                .collect(toList());
        vehicleCategoryTranslationRepository.saveAll(translations);

        Integer total = resolveTotalVehicles(vehicleCategory);
        Integer available = resolveAvailableVehicles(vehicleCategory, total);

        VehicleCategoryDTO response = VehicleCategoryDTO.builder()
                .id(vehicleCategory.getId())
                .code(vehicleCategory.getCode())
                .minSeat(vehicleCategory.getMinSeat())
                .maxSeat(vehicleCategory.getMaxSeat())
                .minLuggage(vehicleCategory.getMinLuggage())
                .maxLuggage(vehicleCategory.getMaxLuggage())
                .unitLuggage(vehicleCategory.getUnitLuggage())
                .active(vehicleCategory.isActive())
                .isDefault(vehicleCategory.isDefault())
                .thumbnailId(vehicleCategory.getThumbnailId())
                .thumbnailUrl(resolveThumbnailUrl(vehicleCategory.getThumbnailId()))
                .totalVehicles(total)
                .availableVehicles(available)
                .translations(
                        translations.stream().map(
                                t -> VehicleCategoryTranslationDTO.builder()
                                        .id(t.getId())
                                        .locale(t.getLocale())
                                        .name(t.getName())
                                        .description(t.getDescription())
                                        .build()
                        )
                                .toList()
                )
                .build();
        return response;
    }

    @Override
    @Cacheable(value = "vehicleCategories", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #search + '_' + #active")
    public Page<VehicleCategoryDTO> getVehicleCategories(Pageable pageable, String search, Boolean active) {

        Page<VehicleCategory> vehicleCategories = vehicleCategoryTranslationRepository.search(search, active != null ? active : true, pageable);
        return vehicleCategories.map(this::convertToDTO);
    }

    @Override
    @Cacheable(value = "vehicleCategories", key = "#id")
    public VehicleCategoryDTO getVehicleCategoryById(Long id) {
        VehicleCategory vehicleCategory = vehicleCategoryRepository.findById(id).orElse(null);
        if (vehicleCategory != null) {
            List<VehicleCategoryTranslationDTO> translations = vehicleCategory.getTranslations().stream()
                    .map(t -> VehicleCategoryTranslationDTO.builder()
                            .id(t.getId())
                            .locale(t.getLocale())
                            .name(t.getName())
                            .slug(t.getSlug())
                            .description(t.getDescription())
                            .build())
                    .collect(Collectors.toList());
            Integer total = resolveTotalVehicles(vehicleCategory);
            Integer available = resolveAvailableVehicles(vehicleCategory, total);
            return VehicleCategoryDTO.builder()
                    .id(vehicleCategory.getId())
                    .code(vehicleCategory.getCode())
                    .minSeat(vehicleCategory.getMinSeat())
                    .maxSeat(vehicleCategory.getMaxSeat())
                    .minLuggage(vehicleCategory.getMinLuggage())
                    .maxLuggage(vehicleCategory.getMaxLuggage())
                    .unitLuggage(vehicleCategory.getUnitLuggage())
                    .active(vehicleCategory.isActive())
                    .isDefault(vehicleCategory.isDefault())
                    .thumbnailId(vehicleCategory.getThumbnailId())
                    .thumbnailUrl(resolveThumbnailUrl(vehicleCategory.getThumbnailId()))
                    .totalVehicles(total)
                    .availableVehicles(available)
                    .translations(translations)
                    .build();
        }
        return null;
    }

    @Override
    @Cacheable(value = "vehicleCategories", key = "'slug_' + #slug")
    public VehicleCategoryDTO getVehicleCategoryBySlug(String slug) {
        if (slug == null || slug.isBlank()) return null;
        return vehicleCategoryTranslationRepository.findBySlug(slug)
                .map(tr -> getVehicleCategoryById(tr.getVehicleCategory().getId()))
                .or(() -> vehicleCategoryRepository.findByCode(slug).map(c -> getVehicleCategoryById(c.getId())))
                .orElseGet(() -> {
                    try {
                        Long id = Long.parseLong(slug);
                        return getVehicleCategoryById(id);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                });
    }

    @Override
    @Transactional
    @CacheEvict(value = "vehicleCategories", allEntries = true)
    public VehicleCategoryDTO updateVehicleCategory(Long id, VehicleCategoryDTO vehicleCategoryDTO) {
        try {
            // 1. Kiểm tra sự tồn tại của bảng cha
            VehicleCategory existingVehicleCategory = vehicleCategoryRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Vehicle Category not found with id: " + id));

            // 2. Cập nhật thông tin bảng cha (chỉ set khi dữ liệu thay đổi hoặc có giá trị mới)
            if (vehicleCategoryDTO.getActive() != null) {
                existingVehicleCategory.setActive(vehicleCategoryDTO.getActive());
            }
            existingVehicleCategory.setMinSeat(vehicleCategoryDTO.getMinSeat());
            existingVehicleCategory.setMaxSeat(vehicleCategoryDTO.getMaxSeat());
            existingVehicleCategory.setMinLuggage(vehicleCategoryDTO.getMinLuggage());
            existingVehicleCategory.setMaxLuggage(vehicleCategoryDTO.getMaxLuggage());
            existingVehicleCategory.setUnitLuggage(vehicleCategoryDTO.getUnitLuggage());
            if (vehicleCategoryDTO.getIsDefault() != null) {
                existingVehicleCategory.setDefault(vehicleCategoryDTO.getIsDefault());
            }
            if (vehicleCategoryDTO.getThumbnailId() != null || vehicleCategoryDTO.getThumbnailUrl() == null) {
                existingVehicleCategory.setThumbnailId(vehicleCategoryDTO.getThumbnailId());
            }

            if (vehicleCategoryDTO.getTotalVehicles() != null) {
                existingVehicleCategory.setTotalVehicles(vehicleCategoryDTO.getTotalVehicles());
            }
            if (vehicleCategoryDTO.getAvailableVehicles() != null) {
                existingVehicleCategory.setAvailableVehicles(vehicleCategoryDTO.getAvailableVehicles());
            }

            if (vehicleCategoryDTO.getCode() != null && !vehicleCategoryDTO.getCode().equals(existingVehicleCategory.getCode())) {
                existingVehicleCategory.setCode(vehicleCategoryDTO.getCode());
            }

            // 3. Cập nhật danh sách bảng con (Translations) với 3-way diff
            if (vehicleCategoryDTO.getTranslations() != null) {
                final VehicleCategory finalCategory = existingVehicleCategory;
                EntityMergeUtils.mergeCollection(
                        existingVehicleCategory.getTranslations(),
                        vehicleCategoryDTO.getTranslations(),
                        t -> t.getLocale() != null ? t.getLocale().toLowerCase() : null,
                        tDto -> tDto.getLocale() != null ? tDto.getLocale().toLowerCase() : null,
                        (existing, tDto) -> {
                            if (tDto.getName() != null) existing.setName(tDto.getName());
                            if (tDto.getSlug() != null) existing.setSlug(tDto.getSlug());
                            if (tDto.getDescription() != null) existing.setDescription(tDto.getDescription());
                        },
                        tDto -> VehicleCategoryTranslation.builder()
                                .vehicleCategory(finalCategory)
                                .locale(tDto.getLocale().toLowerCase())
                                .name(tDto.getName())
                                .slug(tDto.getSlug())
                                .description(tDto.getDescription())
                                .build()
                );
            }

            // 4. Lưu lại (Nhờ @Transactional, thực chất hàm .save này có thể bỏ qua vì Hibernate tự detect thay đổi để update)
            VehicleCategory updatedCategory = vehicleCategoryRepository.save(existingVehicleCategory);

            return convertToDTO(updatedCategory);

        } catch (EntityNotFoundException e) {
            // Log lỗi hoặc ném ra Custom Exception cho Global Exception Handler xử lý để trả về 404
            throw e;
        } catch (Exception e) {
            // Bắt các lỗi runtime khác (ví dụ: trùng lặp code, trùng lặp slug, lỗi kết nối DB, v.v...)
            throw new RuntimeException("Failed to update vehicle category: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "vehicleCategories", allEntries = true)
    public void deleteVehicleCategory(Long id) {
        VehicleCategory vehicleCategory = vehicleCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle Category not found with id: " + id));
        vehicleCategoryRepository.delete(vehicleCategory);
    }


    protected VehicleCategoryDTO convertToDTO(VehicleCategory vehicleCategory) {
        return VehicleCategoryDTO.builder()
                .id(vehicleCategory.getId())
                .code(vehicleCategory.getCode())
                .minSeat(vehicleCategory.getMinSeat())
                .maxSeat(vehicleCategory.getMaxSeat())
                .minLuggage(vehicleCategory.getMinLuggage())
                .maxLuggage(vehicleCategory.getMaxLuggage())
                .unitLuggage(vehicleCategory.getUnitLuggage())
                .active(vehicleCategory.isActive())
                .isDefault(vehicleCategory.isDefault())
                .thumbnailId(vehicleCategory.getThumbnailId())
                .thumbnailUrl(resolveThumbnailUrl(vehicleCategory.getThumbnailId()))
                .translations(vehicleCategory.getTranslations().stream()
                        .map(t -> VehicleCategoryTranslationDTO.builder()
                                .id(t.getId())
                                .locale(t.getLocale())
                                .name(t.getName())
                                .slug(t.getSlug())
                                .description(t.getDescription())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    };
}
