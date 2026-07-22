package com.dotnt.booking_travel_backend.rental.api;

import com.dotnt.booking_travel_backend.rental.api.dtos.RentalDto;
import com.dotnt.booking_travel_backend.rental.internal.entity.RentalPrice;
import com.dotnt.booking_travel_backend.rental.internal.repository.RentalPriceRepository;
import com.dotnt.booking_travel_backend.rental.internal.service.IRentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rentals")
@RequiredArgsConstructor
public class RentalPublicController {
    private final IRentalService rentalService;
    private final RentalPriceRepository rentalPriceRepository;

    @GetMapping
    public ResponseEntity<Page<RentalDto>> list(Pageable pageable) {
        return ResponseEntity.ok(rentalService.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalDto> get(@PathVariable Long id) {
        RentalDto dto = rentalService.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/prices")
    public ResponseEntity<List<RentalPrice>> getPrices(@PathVariable Long id) {
        return ResponseEntity.ok(rentalPriceRepository.findByRentalId(id));
    }
}
