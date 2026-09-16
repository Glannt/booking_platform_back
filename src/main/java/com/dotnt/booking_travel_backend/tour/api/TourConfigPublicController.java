package com.dotnt.booking_travel_backend.tour.api;

import com.dotnt.booking_travel_backend.tour.api.dtos.TourConfigDto;
import com.dotnt.booking_travel_backend.tour.internal.service.TourConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tours/config")
@RequiredArgsConstructor
public class TourConfigPublicController {

    private final TourConfigService tourConfigService;

    @GetMapping
    public ResponseEntity<TourConfigDto> getConfig() {
        return ResponseEntity.ok(tourConfigService.getConfig());
    }
}
