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
import com.dotnt.booking_travel_backend.media.internal.repository.ImageRepository;
import com.dotnt.booking_travel_backend.shared.specs.GenericSpecification;
import com.dotnt.booking_travel_backend.shared.specs.SearchOperation;
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
    private final ImageRepository imageRepository;

    private String resolveThumbnailUrl(Long thumbnailId) {
        if (thumbnailId == null) return null;
        return imageRepository.findById(thumbnailId)
                .map(img -> img.getFilePath())
                .orElse(null);
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
            .luggageWeight(request.luggageWeight())
            .active(request.active() != null ? request.active() : true)
            .isDefault(request.isDefault() != null ? request.isDefault() : false)
            .thumbnailId(request.thumbnailId())
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

        VehicleCategoryDTO response = VehicleCategoryDTO.builder()
                .code(vehicleCategory.getCode())
                .minSeat(vehicleCategory.getMinSeat())
                .maxSeat(vehicleCategory.getMaxSeat())
                .minLuggage(vehicleCategory.getMinLuggage())
                .maxLuggage(vehicleCategory.getMaxLuggage())
                .unitLuggage(vehicleCategory.getUnitLuggage())
                .luggageWeight(vehicleCategory.getLuggageWeight())
                .active(vehicleCategory.isActive())
                .isDefault(vehicleCategory.isDefault())
                .thumbnailId(vehicleCategory.getThumbnailId())
                .thumbnailUrl(resolveThumbnailUrl(vehicleCategory.getThumbnailId()))
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
            return VehicleCategoryDTO.builder()
                    .id(vehicleCategory.getId())
                    .code(vehicleCategory.getCode())
                    .minSeat(vehicleCategory.getMinSeat())
                    .maxSeat(vehicleCategory.getMaxSeat())
                    .minLuggage(vehicleCategory.getMinLuggage())
                    .maxLuggage(vehicleCategory.getMaxLuggage())
                    .unitLuggage(vehicleCategory.getUnitLuggage())
                    .luggageWeight(vehicleCategory.getLuggageWeight())
                    .active(vehicleCategory.isActive())
                    .isDefault(vehicleCategory.isDefault())
                    .thumbnailId(vehicleCategory.getThumbnailId())
                    .thumbnailUrl(resolveThumbnailUrl(vehicleCategory.getThumbnailId()))
                    .translations(translations)
                    .build();
        }
        return null;
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
            existingVehicleCategory.setLuggageWeight(vehicleCategoryDTO.getLuggageWeight());
            if (vehicleCategoryDTO.getIsDefault() != null) {
                existingVehicleCategory.setDefault(vehicleCategoryDTO.getIsDefault());
            }
            if (vehicleCategoryDTO.getThumbnailId() != null || vehicleCategoryDTO.getThumbnailUrl() == null) {
                existingVehicleCategory.setThumbnailId(vehicleCategoryDTO.getThumbnailId());
            }

            if (vehicleCategoryDTO.getCode() != null && !vehicleCategoryDTO.getCode().equals(existingVehicleCategory.getCode())) {
                existingVehicleCategory.setCode(vehicleCategoryDTO.getCode());
            }

            // 3. Cập nhật danh sách bảng con (Translations)
            if (vehicleCategoryDTO.getTranslations() != null && !vehicleCategoryDTO.getTranslations().isEmpty()) {

                // Chuyển danh sách translations hiện tại trong DB thành Map để tìm kiếm nhanh theo locale (O(1))
                Map<String, VehicleCategoryTranslation> existingTranslationsMap = existingVehicleCategory.getTranslations()
                        .stream()
                        .collect(Collectors.toMap(VehicleCategoryTranslation::getLocale, t -> t));

                for (VehicleCategoryTranslationDTO translationDTO : vehicleCategoryDTO.getTranslations()) {
                    String locale = translationDTO.getLocale();

                    if (existingTranslationsMap.containsKey(locale)) {
                        // Trường hợp 1: Đã tồn tại ngôn ngữ này -> Cập nhật thông tin
                        VehicleCategoryTranslation existingTranslation = existingTranslationsMap.get(locale);
                        if (translationDTO.getName() != null) {
                            existingTranslation.setName(translationDTO.getName());
                        }
                        if (translationDTO.getSlug() != null) {
                            existingTranslation.setSlug(translationDTO.getSlug());
                        }
                        if (translationDTO.getDescription() != null) {
                            existingTranslation.setDescription(translationDTO.getDescription());
                        }
                    } else {
                        // Trường hợp 2: Ngôn ngữ mới hoàn toàn -> Tạo mới và map mối quan hệ với cha
                        VehicleCategoryTranslation newTranslation = VehicleCategoryTranslation.builder()
                                .vehicleCategory(existingVehicleCategory) // Gắn quan hệ với cha
                                .locale(locale)
                                .name(translationDTO.getName())
                                .slug(translationDTO.getSlug())
                                .description(translationDTO.getDescription())
                                .build();

                        // Thêm vào list của cha, nhờ `cascade = CascadeType.ALL` nó sẽ tự động lưu xuống DB
                        existingVehicleCategory.getTranslations().add(newTranslation);
                    }
                }
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
                .luggageWeight(vehicleCategory.getLuggageWeight())
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
