package com.dotnt.booking_travel_backend.fleet.api;

import com.dotnt.booking_travel_backend.fleet.api.dtos.PublicAmenityCategoryDTO;
import com.dotnt.booking_travel_backend.fleet.api.dtos.PublicAmenityDTO;
import com.dotnt.booking_travel_backend.fleet.internal.service.IAmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicAmenityController {

    private final IAmenityService amenityService;

    @GetMapping("/amenity-categories")
    public ResponseEntity<List<PublicAmenityCategoryDTO>> getCategories(
            @RequestParam(required = false, defaultValue = "vi") String locale) {
        return ResponseEntity.ok(amenityService.getPublicCategories(locale));
    }

    @GetMapping("/amenities")
    public ResponseEntity<List<PublicAmenityDTO>> getAmenitiesByCategory(
            @RequestParam String categoryCode,
            @RequestParam(required = false, defaultValue = "vi") String locale) {
        return ResponseEntity.ok(amenityService.getPublicAmenitiesByCategory(categoryCode, locale));
    }
}
