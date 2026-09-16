package com.dotnt.booking_travel_backend.fleet.internal.service;

import com.dotnt.booking_travel_backend.fleet.api.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAddOnService {
    // Portal / Admin Category CRUD
    AddOnCategoryDTO createCategory(CreateAddOnCategoryRequest request);
    AddOnCategoryDTO updateCategory(Long id, CreateAddOnCategoryRequest request);
    void deleteCategory(Long id);
    AddOnCategoryDTO getCategoryById(Long id);
    List<AddOnCategoryDTO> getAllCategories(Boolean activeOnly);
    Page<AddOnCategoryDTO> getCategoriesPaged(String keyword, Pageable pageable);

    // Portal / Admin AddOn CRUD
    AddOnDTO createAddOn(CreateAddOnRequest request);
    AddOnDTO updateAddOn(Long id, CreateAddOnRequest request);
    void deleteAddOn(Long id);
    AddOnDTO getAddOnById(Long id);
    List<AddOnDTO> getAddOnsByCategoryId(Long categoryId);
    List<AddOnDTO> getAddOnsByCategoryCode(String categoryCode);

    // Public API
    List<PublicAddOnCategoryDTO> getPublicCategories(String locale, Boolean filterableOnly);
    List<PublicAddOnDTO> getPublicAddOnsByCategory(String categoryCode, String locale);
}
