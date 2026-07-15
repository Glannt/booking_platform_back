package com.dotnt.booking_travel_backend.rental.api;

import com.dotnt.booking_travel_backend.rental.api.dtos.RentalDto;
import com.dotnt.booking_travel_backend.rental.internal.service.IRentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final IRentalService rentalService;

    @PostMapping
    public ResponseEntity<RentalDto> create(@RequestBody RentalDto dto) {
        return ResponseEntity.ok(rentalService.create(dto));
    }

    @GetMapping
    public ResponseEntity<Page<RentalDto>> list(Pageable pageable) {
        return ResponseEntity.ok(rentalService.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalDto> get(@PathVariable Long id) {
        RentalDto dto = rentalService.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<RentalDto> update(@PathVariable Long id, @RequestBody RentalDto dto) {
        RentalDto updated = rentalService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rentalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

