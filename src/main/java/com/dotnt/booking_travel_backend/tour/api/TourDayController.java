package com.dotnt.booking_travel_backend.tour.api;

import com.dotnt.booking_travel_backend.tour.api.dtos.TourDayDto;
import com.dotnt.booking_travel_backend.tour.internal.service.ITourDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tours/{tourId}/days")
@RequiredArgsConstructor
public class TourDayController {
    private final ITourDayService tourDayService;

    @PostMapping
    public ResponseEntity<TourDayDto> create(@PathVariable Long tourId, @RequestBody TourDayDto dto) {
        return ResponseEntity.ok(tourDayService.create(tourId, dto));
    }

    @GetMapping
    public ResponseEntity<Page<TourDayDto>> list(@PathVariable Long tourId, Pageable pageable) {
        return ResponseEntity.ok(tourDayService.list(tourId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourDayDto> get(@PathVariable Long tourId, @PathVariable Long id) {
        TourDayDto dto = tourDayService.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourDayDto> update(@PathVariable Long tourId, @PathVariable Long id, @RequestBody TourDayDto dto) {
        TourDayDto updated = tourDayService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long tourId, @PathVariable Long id) {
        tourDayService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

