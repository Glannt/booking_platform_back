package com.dotnt.booking_travel_backend.tour.internal.service.impl;

import com.dotnt.booking_travel_backend.tour.api.dtos.CreateTourCategoryRequest;
import com.dotnt.booking_travel_backend.tour.api.dtos.TourCategoryDTO;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourCategory;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourCategoryRepository;
import com.dotnt.booking_travel_backend.tour.internal.service.ITourCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TourCategoryServiceImpl implements ITourCategoryService {

    private final TourCategoryRepository tourCategoryRepository;

    @Override
    @Cacheable(value = "tourCategories", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<TourCategoryDTO> getAll(Pageable pageable) {
        return tourCategoryRepository.findAll(pageable)
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
        TourCategory category = TourCategory.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .isActive(request.isActive())
                .build();
        category = tourCategoryRepository.save(category);
        return convertToDTO(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = "tourCategories", allEntries = true)
    public TourCategoryDTO update(Long id, CreateTourCategoryRequest request) {
        TourCategory category = tourCategoryRepository.findById(id).orElse(null);
        if (category != null) {
            category.setTitle(request.getTitle());
            category.setDescription(request.getDescription());
            category.setActive(request.isActive());
            category = tourCategoryRepository.save(category);
            return convertToDTO(category);
        }
        return null;
    }

    @Override
    @Transactional
    @CacheEvict(value = "tourCategories", allEntries = true)
    public void delete(Long id) {
        tourCategoryRepository.deleteById(id);
    }

    private TourCategoryDTO convertToDTO(TourCategory category) {
        return TourCategoryDTO.builder()
                .id(category.getId())
                .title(category.getTitle())
                .description(category.getDescription())
                .isActive(category.isActive())
                .build();
    }
}
