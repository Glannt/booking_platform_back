package com.dotnt.booking_travel_backend.tour.api;

import com.dotnt.booking_travel_backend.tour.api.dtos.TourConfigDto;
import com.dotnt.booking_travel_backend.tour.internal.service.TourConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/tours/config")
@RequiredArgsConstructor
public class TourConfigAdminController {

    private final TourConfigService tourConfigService;

    @GetMapping
    public ResponseEntity<TourConfigDto> getConfig() {
        return ResponseEntity.ok(tourConfigService.getConfig());
    }

    @PutMapping
    @PreAuthorize("hasAuthority('TOUR_UPDATE') or hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<TourConfigDto> updateConfig(@RequestBody TourConfigDto dto) {
        return ResponseEntity.ok(tourConfigService.updateConfig(dto));
    }
}
