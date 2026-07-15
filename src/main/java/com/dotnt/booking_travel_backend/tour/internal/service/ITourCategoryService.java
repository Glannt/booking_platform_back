package com.dotnt.booking_travel_backend.tour.internal.service;

import com.dotnt.booking_travel_backend.tour.api.dtos.CreateTourCategoryRequest;
import com.dotnt.booking_travel_backend.tour.api.dtos.TourCategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITourCategoryService {
    Page<TourCategoryDTO> getAll(Pageable pageable);
    TourCategoryDTO getById(Long id);
    TourCategoryDTO create(CreateTourCategoryRequest request);
    TourCategoryDTO update(Long id, CreateTourCategoryRequest request);
    void delete(Long id);
}
