package com.dotnt.booking_travel_backend.intercity.api;

import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityRouteDto;
import com.dotnt.booking_travel_backend.intercity.internal.service.IIntercityRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/intercity-routes")
@RequiredArgsConstructor
public class IntercityRouteController {

    private final IIntercityRouteService routeService;

    @PostMapping
    @PreAuthorize("hasAuthority('INTERCITY_ROUTE_CREATE')")
    public ResponseEntity<IntercityRouteDto> create(@RequestBody IntercityRouteDto dto) {
        return ResponseEntity.ok(routeService.create(dto));
    }

    @GetMapping
    public ResponseEntity<Page<IntercityRouteDto>> list(Pageable pageable) {
        return ResponseEntity.ok(routeService.list(pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<List<IntercityRouteDto>> listActive() {
        return ResponseEntity.ok(routeService.listActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IntercityRouteDto> get(@PathVariable Long id) {
        IntercityRouteDto dto = routeService.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<IntercityRouteDto> getBySlug(@PathVariable String slug) {
        IntercityRouteDto dto = routeService.getBySlug(slug);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INTERCITY_ROUTE_UPDATE')")
    public ResponseEntity<IntercityRouteDto> update(@PathVariable Long id, @RequestBody IntercityRouteDto dto) {
        IntercityRouteDto updated = routeService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INTERCITY_ROUTE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
