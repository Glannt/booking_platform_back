package com.dotnt.booking_travel_backend.tour.internal.service.impl;

import com.dotnt.booking_travel_backend.shared.utils.EntityMergeUtils;
import com.dotnt.booking_travel_backend.tour.api.dtos.CreateTourCategoryRequest;
import com.dotnt.booking_travel_backend.tour.api.dtos.TourCategoryDTO;
import com.dotnt.booking_travel_backend.tour.api.dtos.TourCategoryTranslationDTO;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourCategory;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourCategoryTranslation;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourCategoryRepository;
import com.dotnt.booking_travel_backend.tour.internal.service.ITourCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourCategoryServiceImpl implements ITourCategoryService {

    private final TourCategoryRepository tourCategoryRepository;

    @Override
    @Cacheable(value = "tourCategories", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort")
    public Page<TourCategoryDTO> getAll(Pageable pageable) {
        return tourCategoryRepository.findAllByOrderBySortOrderAsc(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Cacheable(value = "tourCategories", key = "#id")
    public TourCategoryDTO getById(Long id) {
        return tourCategoryRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "tourCategories", allEntries = true)
    public TourCategoryDTO create(CreateTourCategoryRequest request) {

        String code = request.getCode();
        if (code == null || code.isBlank()) {
            code = "cat-" + System.currentTimeMillis();
        }

        TourCategory category = TourCategory.builder()
                .code(code)
                .icon(request.getIcon() != null ? request.getIcon() : "compass")
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive())
                .title(request.getTitle())
                .description(request.getDescription())
                .translations(new ArrayList<>())
                .build();

        if (request.getTranslations() != null && !request.getTranslations().isEmpty()) {
            for (TourCategoryTranslationDTO tDto : request.getTranslations()) {
                if (tDto.getLocale() == null || tDto.getName() == null) continue;
                String slug = tDto.getSlug();
                if (slug == null || slug.isBlank()) {
                    slug = tDto.getName().toLowerCase().replaceAll("[^a-zA-Z0-9-]", "-");
                }
                TourCategoryTranslation translation = TourCategoryTranslation.builder()
                        .tourCategory(category)
                        .locale(tDto.getLocale().toLowerCase())
                        .name(tDto.getName())
                        .slug(slug)
                        .description(tDto.getDescription())
                        .build();
                category.getTranslations().add(translation);

                if ("vi".equalsIgnoreCase(tDto.getLocale()) && (category.getTitle() == null || category.getTitle().isBlank())) {
                    category.setTitle(tDto.getName());
                    category.setDescription(tDto.getDescription());
                }
            }
        }

        category = tourCategoryRepository.save(category);
        return convertToDTO(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = "tourCategories", allEntries = true)
    public TourCategoryDTO update(Long id, CreateTourCategoryRequest request) {
        TourCategory category = tourCategoryRepository.findById(id).orElse(null);
        if (category == null) {
            return null;
        }

        if (request.getCode() != null && !request.getCode().isBlank()) {
            category.setCode(request.getCode());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            category.setActive(request.getIsActive());
        }
        if (request.getTitle() != null) {
            category.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getTranslations() != null) {
            final TourCategory finalCategory = category;
            EntityMergeUtils.mergeCollection(
                    category.getTranslations(),
                    request.getTranslations(),
                    t -> t.getLocale() != null ? t.getLocale().toLowerCase() : null,
                    tDto -> tDto.getLocale() != null ? tDto.getLocale().toLowerCase() : null,
                    (existing, tDto) -> {
                        existing.setName(tDto.getName());
                        String slug = tDto.getSlug();
                        if (slug == null || slug.isBlank()) {
                            slug = tDto.getName() != null ? tDto.getName().toLowerCase().replaceAll("[^a-zA-Z0-9-]", "-") : "";
                        }
                        existing.setSlug(slug);
                        existing.setDescription(tDto.getDescription());
                        if ("vi".equalsIgnoreCase(tDto.getLocale())) {
                            finalCategory.setTitle(tDto.getName());
                            finalCategory.setDescription(tDto.getDescription());
                        }
                    },
                    tDto -> {
                        String slug = tDto.getSlug();
                        if (slug == null || slug.isBlank()) {
                            slug = tDto.getName() != null ? tDto.getName().toLowerCase().replaceAll("[^a-zA-Z0-9-]", "-") : "";
                        }
                        if ("vi".equalsIgnoreCase(tDto.getLocale())) {
                            finalCategory.setTitle(tDto.getName());
                            finalCategory.setDescription(tDto.getDescription());
                        }
                        return TourCategoryTranslation.builder()
                                .tourCategory(finalCategory)
                                .locale(tDto.getLocale().toLowerCase())
                                .name(tDto.getName())
                                .slug(slug)
                                .description(tDto.getDescription())
                                .build();
                    }
            );
        }

        category = tourCategoryRepository.save(category);
        return convertToDTO(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = "tourCategories", allEntries = true)
    public void delete(Long id) {
        tourCategoryRepository.deleteById(id);
    }

    private TourCategoryDTO convertToDTO(TourCategory category) {
        List<TourCategoryTranslationDTO> transDtos = category.getTranslations().stream()
                .map(t -> TourCategoryTranslationDTO.builder()
                        .id(t.getId())
                        .locale(t.getLocale())
                        .name(t.getName())
                        .slug(t.getSlug())
                        .description(t.getDescription())
                        .build())
                .collect(Collectors.toList());

        String resolvedTitle = category.getTitle();
        String resolvedDesc = category.getDescription();
        if ((resolvedTitle == null || resolvedTitle.isBlank()) && !transDtos.isEmpty()) {
            TourCategoryTranslationDTO viTrans = transDtos.stream()
                    .filter(t -> "vi".equalsIgnoreCase(t.getLocale()))
                    .findFirst()
                    .orElse(transDtos.get(0));
            resolvedTitle = viTrans.getName();
            resolvedDesc = viTrans.getDescription();
        }

        return TourCategoryDTO.builder()
                .id(category.getId())
                .code(category.getCode())
                .icon(category.getIcon())
                .sortOrder(category.getSortOrder() != null ? category.getSortOrder() : 0)
                .active(category.isActive())
                .isActive(category.isActive())
                .title(resolvedTitle)
                .description(resolvedDesc)
                .translations(transDtos)
                .build();
    }
}
