package com.dotnt.booking_travel_backend.tour.api;

import com.dotnt.booking_travel_backend.tour.api.dtos.CreateTourRequest;
import com.dotnt.booking_travel_backend.tour.api.dtos.TourDto;
import com.dotnt.booking_travel_backend.tour.internal.service.ITourService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tours")
@RequiredArgsConstructor
public class TourController {
    private final ITourService tourService;

    @PreAuthorize("hasAuthority('TOUR_CREATE')")
    @PostMapping
    public ResponseEntity<TourDto> create(@RequestBody CreateTourRequest req) {
        return ResponseEntity.ok(tourService.createTour(req));
    }

    @GetMapping
    public ResponseEntity<Page<TourDto>> list(Pageable pageable, 
                                              @RequestParam(required = false) String search,
                                              @RequestParam(required = false) Long tourCategoryId) {
        return ResponseEntity.ok(tourService.getTours(pageable, search, tourCategoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourDto> get(@PathVariable Long id) {
        TourDto dto = tourService.getTour(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAuthority('TOUR_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<TourDto> update(@PathVariable Long id, @RequestBody TourDto dto) {
        TourDto updated = tourService.updateTour(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAuthority('TOUR_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tourService.deleteTour(id);
        return ResponseEntity.noContent().build();
    }
}

