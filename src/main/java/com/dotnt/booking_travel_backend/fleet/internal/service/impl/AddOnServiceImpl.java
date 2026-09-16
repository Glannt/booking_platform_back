package com.dotnt.booking_travel_backend.fleet.internal.service.impl;

import com.dotnt.booking_travel_backend.fleet.api.dtos.*;
import com.dotnt.booking_travel_backend.fleet.internal.entity.*;
import com.dotnt.booking_travel_backend.fleet.internal.repository.*;
import com.dotnt.booking_travel_backend.fleet.internal.service.IAddOnService;
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
public class AddOnServiceImpl implements IAddOnService {

    private final AddOnCategoryRepository categoryRepository;
    private final AddOnRepository addOnRepository;

    @Override
    @Transactional
    public AddOnCategoryDTO createCategory(CreateAddOnCategoryRequest request) {
        AddOnCategory category = AddOnCategory.builder()
                .code(request.code().trim().toUpperCase())
                .icon(request.icon())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .active(request.active() != null ? request.active() : true)
                .isFilterable(request.isFilterable() != null ? request.isFilterable() : false)
                .build();

        if (request.translations() != null) {
            for (AddOnCategoryTranslationDTO tDto : request.translations()) {
                AddOnCategoryTranslation translation = AddOnCategoryTranslation.builder()
                        .addonCategory(category)
                        .locale(tDto.getLocale().toLowerCase())
                        .name(tDto.getName())
                        .description(tDto.getDescription())
                        .build();
                category.addTranslation(translation);
            }
        }

        AddOnCategory saved = categoryRepository.save(category);
        return toCategoryDTO(saved);
    }

    @Override
    @Transactional
    public AddOnCategoryDTO updateCategory(Long id, CreateAddOnCategoryRequest request) {
        AddOnCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AddOnCategory not found with id: " + id));

        if (request.code() != null) category.setCode(request.code().trim().toUpperCase());
        if (request.icon() != null) category.setIcon(request.icon());
        if (request.displayOrder() != null) category.setDisplayOrder(request.displayOrder());
        if (request.active() != null) category.setActive(request.active());
        if (request.isFilterable() != null) category.setFilterable(request.isFilterable());

        if (request.translations() != null) {
            final AddOnCategory finalCategory = category;
            EntityMergeUtils.mergeCollection(
                    category.getTranslations(),
                    request.translations(),
                    t -> t.getLocale() != null ? t.getLocale().toLowerCase() : null,
                    tDto -> tDto.getLocale() != null ? tDto.getLocale().toLowerCase() : null,
                    (existing, tDto) -> {
                        existing.setName(tDto.getName());
                        existing.setDescription(tDto.getDescription());
                    },
                    tDto -> AddOnCategoryTranslation.builder()
                            .addonCategory(finalCategory)
                            .locale(tDto.getLocale().toLowerCase())
                            .name(tDto.getName())
                            .description(tDto.getDescription())
                            .build()
            );
        }

        AddOnCategory updated = categoryRepository.save(category);
        return toCategoryDTO(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public AddOnCategoryDTO getCategoryById(Long id) {
        return categoryRepository.findById(id).map(this::toCategoryDTO).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddOnCategoryDTO> getAllCategories(Boolean activeOnly) {
        List<AddOnCategory> list = (activeOnly != null && activeOnly)
                ? categoryRepository.findByActiveTrueOrderByDisplayOrderAsc()
                : categoryRepository.findAll();
        return list.stream().map(this::toCategoryDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AddOnCategoryDTO> getCategoriesPaged(String keyword, Pageable pageable) {
        Page<AddOnCategory> page = (keyword != null && !keyword.isBlank())
                ? categoryRepository.findByCodeContainingIgnoreCase(keyword.trim(), pageable)
                : categoryRepository.findAll(pageable);
        return page.map(this::toCategoryDTO);
    }

    @Override
    @Transactional
    public AddOnDTO createAddOn(CreateAddOnRequest request) {
        AddOnCategory category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.categoryId()));
        } else if (request.categoryCode() != null) {
            category = categoryRepository.findByCode(request.categoryCode().trim().toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Category not found with code: " + request.categoryCode()));
        } else {
            throw new IllegalArgumentException("Category id or code must be provided");
        }

        AddOn addOn = AddOn.builder()
                .category(category)
                .code(request.code().trim().toUpperCase())
                .value(request.value())
                .icon(request.icon())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .active(request.active() != null ? request.active() : true)
                .build();

        if (request.translations() != null) {
            for (AddOnTranslationDTO tDto : request.translations()) {
                AddOnTranslation translation = AddOnTranslation.builder()
                        .addOn(addOn)
                        .locale(tDto.getLocale().toLowerCase())
                        .name(tDto.getName())
                        .description(tDto.getDescription())
                        .build();
                addOn.addTranslation(translation);
            }
        }

        AddOn saved = addOnRepository.save(addOn);
        return toAddOnDTO(saved);
    }

    @Override
    @Transactional
    public AddOnDTO updateAddOn(Long id, CreateAddOnRequest request) {
        AddOn addOn = addOnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AddOn not found with id: " + id));

        if (request.categoryId() != null && !request.categoryId().equals(addOn.getCategory().getId())) {
            AddOnCategory category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.categoryId()));
            addOn.setCategory(category);
        }
        if (request.code() != null) addOn.setCode(request.code().trim().toUpperCase());
        if (request.value() != null) addOn.setValue(request.value());
        if (request.icon() != null) addOn.setIcon(request.icon());
        if (request.displayOrder() != null) addOn.setDisplayOrder(request.displayOrder());
        if (request.active() != null) addOn.setActive(request.active());

        if (request.translations() != null) {
            final AddOn finalAddOn = addOn;
            EntityMergeUtils.mergeCollection(
                    addOn.getTranslations(),
                    request.translations(),
                    t -> t.getLocale() != null ? t.getLocale().toLowerCase() : null,
                    tDto -> tDto.getLocale() != null ? tDto.getLocale().toLowerCase() : null,
                    (existing, tDto) -> {
                        existing.setName(tDto.getName());
                        existing.setDescription(tDto.getDescription());
                    },
                    tDto -> AddOnTranslation.builder()
                            .addOn(finalAddOn)
                            .locale(tDto.getLocale().toLowerCase())
                            .name(tDto.getName())
                            .description(tDto.getDescription())
                            .build()
            );
        }

        AddOn updated = addOnRepository.save(addOn);
        return toAddOnDTO(updated);
    }

    @Override
    @Transactional
    public void deleteAddOn(Long id) {
        addOnRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public AddOnDTO getAddOnById(Long id) {
        return addOnRepository.findById(id).map(this::toAddOnDTO).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddOnDTO> getAddOnsByCategoryId(Long categoryId) {
        return addOnRepository.findByCategoryId(categoryId).stream()
                .map(this::toAddOnDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddOnDTO> getAddOnsByCategoryCode(String categoryCode) {
        return addOnRepository.findByCategoryCode(categoryCode).stream()
                .map(this::toAddOnDTO)
                .collect(Collectors.toList());
    }

    // Public localized API implementations
    @Override
    @Transactional(readOnly = true)
    public List<PublicAddOnCategoryDTO> getPublicCategories(String locale, Boolean filterableOnly) {
        List<AddOnCategory> categories = (filterableOnly != null && filterableOnly)
                ? categoryRepository.findByActiveTrueAndIsFilterableTrueOrderByDisplayOrderAsc()
                : categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();

        return categories.stream().map(cat -> {
            AddOnCategoryTranslation trans = cat.getTranslation(locale);
            String name = trans != null ? trans.getName() : cat.getCode();
            String desc = trans != null ? trans.getDescription() : null;

            List<PublicAddOnDTO> publicAddOns = cat.getAddOns().stream()
                    .filter(AddOn::isActive)
                    .map(ao -> {
                        AddOnTranslation aoTrans = ao.getTranslation(locale);
                        return PublicAddOnDTO.builder()
                                .id(ao.getId())
                                .categoryCode(cat.getCode())
                                .code(ao.getCode())
                                .name(aoTrans != null ? aoTrans.getName() : (ao.getValue() != null ? ao.getValue() : ao.getCode()))
                                .description(aoTrans != null ? aoTrans.getDescription() : null)
                                .value(ao.getValue())
                                .icon(ao.getIcon() != null ? ao.getIcon() : cat.getIcon())
                                .displayOrder(ao.getDisplayOrder())
                                .build();
                    })
                    .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                    .collect(Collectors.toList());

            return PublicAddOnCategoryDTO.builder()
                    .id(cat.getId())
                    .code(cat.getCode())
                    .name(name)
                    .description(desc)
                    .icon(cat.getIcon())
                    .displayOrder(cat.getDisplayOrder())
                    .isFilterable(cat.isFilterable())
                    .addOns(publicAddOns)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicAddOnDTO> getPublicAddOnsByCategory(String categoryCode, String locale) {
        return addOnRepository.findByCategoryCodeAndActiveTrueOrderByDisplayOrderAsc(categoryCode).stream()
                .map(ao -> {
                    AddOnTranslation trans = ao.getTranslation(locale);
                    return PublicAddOnDTO.builder()
                            .id(ao.getId())
                            .categoryCode(ao.getCategory().getCode())
                            .code(ao.getCode())
                            .name(trans != null ? trans.getName() : (ao.getValue() != null ? ao.getValue() : ao.getCode()))
                            .description(trans != null ? trans.getDescription() : null)
                            .value(ao.getValue())
                            .icon(ao.getIcon() != null ? ao.getIcon() : ao.getCategory().getIcon())
                            .displayOrder(ao.getDisplayOrder())
                            .build();
                }).collect(Collectors.toList());
    }

    // Helper mappers
    public AddOnCategoryDTO toCategoryDTO(AddOnCategory category) {
        if (category == null) return null;
        List<AddOnCategoryTranslationDTO> tDtos = category.getTranslations() != null
                ? category.getTranslations().stream().map(t -> AddOnCategoryTranslationDTO.builder()
                        .id(t.getId())
                        .locale(t.getLocale())
                        .name(t.getName())
                        .description(t.getDescription())
                        .build()).collect(Collectors.toList())
                : new ArrayList<>();

        List<AddOnDTO> addOnDtos = category.getAddOns() != null
                ? category.getAddOns().stream().map(this::toAddOnDTO).collect(Collectors.toList())
                : new ArrayList<>();

        return AddOnCategoryDTO.builder()
                .id(category.getId())
                .code(category.getCode())
                .icon(category.getIcon())
                .displayOrder(category.getDisplayOrder())
                .active(category.isActive())
                .isFilterable(category.isFilterable())
                .translations(tDtos)
                .addOns(addOnDtos)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .createdBy(category.getCreatedBy())
                .build();
    }

    public AddOnDTO toAddOnDTO(AddOn addOn) {
        if (addOn == null) return null;
        List<AddOnTranslationDTO> tDtos = addOn.getTranslations() != null
                ? addOn.getTranslations().stream().map(t -> AddOnTranslationDTO.builder()
                        .id(t.getId())
                        .locale(t.getLocale())
                        .name(t.getName())
                        .description(t.getDescription())
                        .build()).collect(Collectors.toList())
                : new ArrayList<>();

        return AddOnDTO.builder()
                .id(addOn.getId())
                .categoryId(addOn.getCategory() != null ? addOn.getCategory().getId() : null)
                .categoryCode(addOn.getCategory() != null ? addOn.getCategory().getCode() : null)
                .code(addOn.getCode())
                .value(addOn.getValue())
                .icon(addOn.getIcon())
                .displayOrder(addOn.getDisplayOrder())
                .active(addOn.isActive())
                .translations(tDtos)
                .createdAt(addOn.getCreatedAt())
                .updatedAt(addOn.getUpdatedAt())
                .createdBy(addOn.getCreatedBy())
                .build();
    }
}
