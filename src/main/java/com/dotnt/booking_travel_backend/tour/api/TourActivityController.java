package com.dotnt.booking_travel_backend.tour.api;

import com.dotnt.booking_travel_backend.tour.api.dtos.TourActivityDto;
import com.dotnt.booking_travel_backend.tour.internal.service.ITourActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tours/{tourId}/days/{dayId}/activities")
@RequiredArgsConstructor
public class TourActivityController {
    private final ITourActivityService activityService;

    @PreAuthorize("hasAuthority('TOUR_CREATE')")
    @PostMapping
    public ResponseEntity<TourActivityDto> create(@PathVariable Long dayId, @RequestBody TourActivityDto dto) {
        return ResponseEntity.ok(activityService.create(dayId, dto));
    }

    @GetMapping
    public ResponseEntity<Page<TourActivityDto>> list(@PathVariable Long dayId, Pageable pageable) {
        return ResponseEntity.ok(activityService.list(dayId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourActivityDto> get(@PathVariable Long dayId, @PathVariable Long id) {
        TourActivityDto dto = activityService.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAuthority('TOUR_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<TourActivityDto> update(@PathVariable Long dayId, @PathVariable Long id, @RequestBody TourActivityDto dto) {
        TourActivityDto updated = activityService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAuthority('TOUR_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long dayId, @PathVariable Long id) {
        activityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

