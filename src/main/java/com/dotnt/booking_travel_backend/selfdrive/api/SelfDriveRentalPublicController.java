package com.dotnt.booking_travel_backend.selfdrive.api;

import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDriveRentalDto;
import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveRentalPrice;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDriveRentalPriceRepository;
import com.dotnt.booking_travel_backend.selfdrive.internal.service.ISelfDriveRentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/self-drive-rentals", "/api/v1/rentals"})
@RequiredArgsConstructor
public class SelfDriveRentalPublicController {
    private final ISelfDriveRentalService rentalService;
    private final SelfDriveRentalPriceRepository rentalPriceRepository;

    @GetMapping
    public ResponseEntity<Page<SelfDriveRentalDto>> list(Pageable pageable) {
        return ResponseEntity.ok(rentalService.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SelfDriveRentalDto> get(@PathVariable Long id) {
        SelfDriveRentalDto dto = rentalService.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/prices")
    public ResponseEntity<List<SelfDriveRentalPrice>> getPrices(@PathVariable Long id) {
        return ResponseEntity.ok(rentalPriceRepository.findByRentalId(id));
    }
}
