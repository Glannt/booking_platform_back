package com.dotnt.booking_travel_backend.rental.api;

import com.dotnt.booking_travel_backend.rental.api.dtos.RentalDto;
import com.dotnt.booking_travel_backend.rental.internal.entity.RentalPrice;
import com.dotnt.booking_travel_backend.rental.internal.repository.RentalPriceRepository;
import com.dotnt.booking_travel_backend.rental.internal.service.IRentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final IRentalService rentalService;
    private final RentalPriceRepository rentalPriceRepository;

    @PostMapping
    @PreAuthorize("hasAuthority('VEHICLE_CREATE')")
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
    @PreAuthorize("hasAuthority('VEHICLE_UPDATE')")
    public ResponseEntity<RentalDto> update(@PathVariable Long id, @RequestBody RentalDto dto) {
        RentalDto updated = rentalService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rentalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/prices")
    public ResponseEntity<List<RentalPrice>> getPrices(@PathVariable Long id) {
        return ResponseEntity.ok(rentalPriceRepository.findByRentalId(id));
    }
}

