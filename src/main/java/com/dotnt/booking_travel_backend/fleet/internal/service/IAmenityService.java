package com.dotnt.booking_travel_backend.fleet.internal.service;

import com.dotnt.booking_travel_backend.fleet.api.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAmenityService {
    // Portal / Admin Category CRUD
    AmenityCategoryDTO createCategory(CreateAmenityCategoryRequest request);
    AmenityCategoryDTO updateCategory(Long id, CreateAmenityCategoryRequest request);
    void deleteCategory(Long id);
    AmenityCategoryDTO getCategoryById(Long id);
    List<AmenityCategoryDTO> getAllCategories(Boolean activeOnly);
    Page<AmenityCategoryDTO> getCategoriesPaged(String keyword, Pageable pageable);

    // Portal / Admin Amenity CRUD
    AmenityDTO createAmenity(CreateAmenityRequest request);
    AmenityDTO updateAmenity(Long id, CreateAmenityRequest request);
    void deleteAmenity(Long id);
    AmenityDTO getAmenityById(Long id);
    List<AmenityDTO> getAmenitiesByCategoryId(Long categoryId);
    List<AmenityDTO> getAmenitiesByCategoryCode(String categoryCode);

    // Public API
    List<PublicAmenityCategoryDTO> getPublicCategories(String locale);
    List<PublicAmenityDTO> getPublicAmenitiesByCategory(String categoryCode, String locale);
}
