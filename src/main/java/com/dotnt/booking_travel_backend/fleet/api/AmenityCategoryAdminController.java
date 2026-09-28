package com.dotnt.booking_travel_backend.fleet.api;

import com.dotnt.booking_travel_backend.fleet.api.dtos.AmenityCategoryDTO;
import com.dotnt.booking_travel_backend.fleet.api.dtos.CreateAmenityCategoryRequest;
import com.dotnt.booking_travel_backend.fleet.internal.service.IAmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/amenity-categories")
@RequiredArgsConstructor
public class AmenityCategoryAdminController {

    private final IAmenityService amenityService;

    @PostMapping
    @PreAuthorize("hasAuthority('AMENITY_CATEGORY_CREATE')")
    public ResponseEntity<AmenityCategoryDTO> create(@RequestBody CreateAmenityCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(amenityService.createCategory(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('AMENITY_CATEGORY_UPDATE')")
    public ResponseEntity<AmenityCategoryDTO> update(@PathVariable Long id, @RequestBody CreateAmenityCategoryRequest request) {
        return ResponseEntity.ok(amenityService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('AMENITY_CATEGORY_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        amenityService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AmenityCategoryDTO> getById(@PathVariable Long id) {
        AmenityCategoryDTO dto = amenityService.getCategoryById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<AmenityCategoryDTO>> getAll(@RequestParam(required = false) Boolean activeOnly) {
        return ResponseEntity.ok(amenityService.getAllCategories(activeOnly));
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<AmenityCategoryDTO>> getPaged(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(amenityService.getCategoriesPaged(search, pageable));
    }
}
