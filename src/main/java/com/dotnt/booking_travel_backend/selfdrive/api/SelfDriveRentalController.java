package com.dotnt.booking_travel_backend.selfdrive.api;

import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDriveRentalDto;
import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveRentalPrice;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDriveRentalPriceRepository;
import com.dotnt.booking_travel_backend.selfdrive.internal.service.ISelfDriveRentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/self-drive-rentals", "/api/v1/admin/rentals"})
@RequiredArgsConstructor
public class SelfDriveRentalController {
    private final ISelfDriveRentalService rentalService;
    private final SelfDriveRentalPriceRepository rentalPriceRepository;

    @PostMapping
    @PreAuthorize("hasAuthority('VEHICLE_CREATE')")
    public ResponseEntity<SelfDriveRentalDto> create(@RequestBody SelfDriveRentalDto dto) {
        return ResponseEntity.ok(rentalService.create(dto));
    }

    @GetMapping
    public ResponseEntity<Page<SelfDriveRentalDto>> list(Pageable pageable) {
        return ResponseEntity.ok(rentalService.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SelfDriveRentalDto> get(@PathVariable Long id) {
        SelfDriveRentalDto dto = rentalService.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_UPDATE')")
    public ResponseEntity<SelfDriveRentalDto> update(@PathVariable Long id, @RequestBody SelfDriveRentalDto dto) {
        SelfDriveRentalDto updated = rentalService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rentalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/prices")
    public ResponseEntity<List<SelfDriveRentalPrice>> getPrices(@PathVariable Long id) {
        return ResponseEntity.ok(rentalPriceRepository.findByRentalId(id));
    }

    @PostMapping("/{id}/prices")
    @PreAuthorize("hasAuthority('VEHICLE_UPDATE')")
    public ResponseEntity<SelfDriveRentalPrice> createPrice(@PathVariable Long id, @RequestBody SelfDriveRentalPrice price) {
        price.setRentalId(id);
        if (price.getCurrency() == null || price.getCurrency().isBlank()) {
            price.setCurrency("VND");
        }
        return ResponseEntity.ok(rentalPriceRepository.save(price));
    }

    @PutMapping("/{id}/prices/{priceId}")
    @PreAuthorize("hasAuthority('VEHICLE_UPDATE')")
    public ResponseEntity<SelfDriveRentalPrice> updatePrice(@PathVariable Long id, @PathVariable Long priceId, @RequestBody SelfDriveRentalPrice price) {
        SelfDriveRentalPrice existing = rentalPriceRepository.findById(priceId).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();
        existing.setVehicleCategoryId(price.getVehicleCategoryId());
        if (price.getCurrency() != null) existing.setCurrency(price.getCurrency());
        existing.setAmount(price.getAmount());
        existing.setActive(price.isActive());
        return ResponseEntity.ok(rentalPriceRepository.save(existing));
    }

    @DeleteMapping("/{id}/prices/{priceId}")
    @PreAuthorize("hasAuthority('VEHICLE_DELETE')")
    public ResponseEntity<Void> deletePrice(@PathVariable Long id, @PathVariable Long priceId) {
        rentalPriceRepository.deleteById(priceId);
        return ResponseEntity.noContent().build();
    }
}
