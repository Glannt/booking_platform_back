package com.dotnt.booking_travel_backend.sharedroute.controller;

import com.dotnt.booking_travel_backend.sharedroute.dto.SharedRouteDirectionDto;
import com.dotnt.booking_travel_backend.sharedroute.dto.SharedRouteDto;
import com.dotnt.booking_travel_backend.sharedroute.dto.SharedRouteScheduleDto;
import com.dotnt.booking_travel_backend.sharedroute.service.ISharedRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shared-routes")
@RequiredArgsConstructor
public class SharedRouteController {

    private final ISharedRouteService routeService;

    @GetMapping
    public ResponseEntity<List<SharedRouteDto>> getAll(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ) {
        return ResponseEntity.ok(routeService.getAllRoutes(activeOnly));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SharedRouteDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<SharedRouteDto> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(routeService.getRouteBySlug(slug));
    }

    @PostMapping
    public ResponseEntity<SharedRouteDto> create(@RequestBody SharedRouteDto dto) {
        return ResponseEntity.ok(routeService.createRoute(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SharedRouteDto> update(@PathVariable Long id, @RequestBody SharedRouteDto dto) {
        return ResponseEntity.ok(routeService.updateRoute(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/directions/{directionId}")
    public ResponseEntity<SharedRouteDirectionDto> updateDirection(
            @PathVariable Long directionId,
            @RequestBody SharedRouteDirectionDto dto
    ) {
        return ResponseEntity.ok(routeService.updateDirection(directionId, dto));
    }

    @PostMapping("/directions/{directionId}/schedules")
    public ResponseEntity<SharedRouteScheduleDto> addSchedule(
            @PathVariable Long directionId,
            @RequestBody SharedRouteScheduleDto dto
    ) {
        return ResponseEntity.ok(routeService.addSchedule(directionId, dto));
    }

    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId) {
        routeService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }
}
