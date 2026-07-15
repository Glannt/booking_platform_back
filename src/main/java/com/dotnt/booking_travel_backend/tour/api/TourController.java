package com.dotnt.booking_travel_backend.tour.api;

import com.dotnt.booking_travel_backend.tour.api.dtos.CreateTourRequest;
import com.dotnt.booking_travel_backend.tour.api.dtos.TourDto;
import com.dotnt.booking_travel_backend.tour.internal.service.ITourService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {
    private final ITourService tourService;

    @PostMapping
    public ResponseEntity<TourDto> create(@RequestBody CreateTourRequest req) {
        return ResponseEntity.ok(tourService.createTour(req));
    }

    @GetMapping
    public ResponseEntity<Page<TourDto>> list(Pageable pageable, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(tourService.getTours(pageable, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourDto> get(@PathVariable Long id) {
        TourDto dto = tourService.getTour(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourDto> update(@PathVariable Long id, @RequestBody TourDto dto) {
        TourDto updated = tourService.updateTour(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tourService.deleteTour(id);
        return ResponseEntity.noContent().build();
    }
}

