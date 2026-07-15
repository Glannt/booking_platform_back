package com.dotnt.booking_travel_backend.tour.api;

import com.dotnt.booking_travel_backend.tour.api.dtos.CreateTourCategoryRequest;
import com.dotnt.booking_travel_backend.tour.api.dtos.TourCategoryDTO;
import com.dotnt.booking_travel_backend.tour.internal.service.ITourCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tour-categories")
@RequiredArgsConstructor
public class TourCategoryController {

    private final ITourCategoryService tourCategoryService;

    @GetMapping
    public ResponseEntity<Page<TourCategoryDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(tourCategoryService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourCategoryDTO> getById(@PathVariable Long id) {
        TourCategoryDTO dto = tourCategoryService.getById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAuthority('TOUR_CATEGORY_CREATE')")
    @PostMapping
    public ResponseEntity<TourCategoryDTO> create(@RequestBody CreateTourCategoryRequest request) {
        return ResponseEntity.ok(tourCategoryService.create(request));
    }

    @PreAuthorize("hasAuthority('TOUR_CATEGORY_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<TourCategoryDTO> update(@PathVariable Long id, @RequestBody CreateTourCategoryRequest request) {
        TourCategoryDTO updated = tourCategoryService.update(id, request);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAuthority('TOUR_CATEGORY_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tourCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
