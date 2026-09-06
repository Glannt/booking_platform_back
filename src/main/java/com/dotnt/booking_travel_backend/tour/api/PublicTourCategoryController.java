package com.dotnt.booking_travel_backend.tour.api;

import com.dotnt.booking_travel_backend.tour.api.dtos.TourCategoryDTO;
import com.dotnt.booking_travel_backend.tour.internal.service.ITourCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/tour-categories")
@RequiredArgsConstructor
public class PublicTourCategoryController {

    private final ITourCategoryService tourCategoryService;

    @GetMapping
    public ResponseEntity<Page<TourCategoryDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(tourCategoryService.getAll(pageable));
    }
}
