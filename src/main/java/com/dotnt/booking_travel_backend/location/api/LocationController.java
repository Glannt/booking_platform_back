package com.dotnt.booking_travel_backend.location.api;

import com.dotnt.booking_travel_backend.location.api.dtos.LocationDto;
import com.dotnt.booking_travel_backend.location.api.dtos.LocationSearchResultDto;
import com.dotnt.booking_travel_backend.location.internal.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/search")
    public List<LocationSearchResultDto> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "vi") String locale,
            @RequestParam(defaultValue = "5") int limit) {
        return locationService.search(q, locale, limit);
    }

    @GetMapping("/active")
    public List<LocationDto> listActive(
            @RequestParam(defaultValue = "vi") String locale) {
        return locationService.listActive(locale);
    }

    @GetMapping("/{id}")
    public LocationDto get(
            @PathVariable Long id,
            @RequestParam(defaultValue = "vi") String locale) {
        return locationService.get(id, locale);
    }
}
