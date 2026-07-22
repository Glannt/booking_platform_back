package com.dotnt.booking_travel_backend.rental.api;

import com.dotnt.booking_travel_backend.rental.api.dtos.RentalTypeDto;
import com.dotnt.booking_travel_backend.rental.internal.service.IRentalTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rental-types")
@RequiredArgsConstructor
public class RentalTypePublicController {
    private final IRentalTypeService service;

    @GetMapping
    public ResponseEntity<Page<RentalTypeDto>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalTypeDto> get(@PathVariable Long id) {
        RentalTypeDto dto = service.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }
}
