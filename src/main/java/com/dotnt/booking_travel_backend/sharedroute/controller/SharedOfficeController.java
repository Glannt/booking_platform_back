package com.dotnt.booking_travel_backend.sharedroute.controller;

import com.dotnt.booking_travel_backend.sharedroute.dto.SharedOfficeDto;
import com.dotnt.booking_travel_backend.sharedroute.service.ISharedOfficeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shared-offices")
@RequiredArgsConstructor
public class SharedOfficeController {

    private final ISharedOfficeService officeService;

    @GetMapping
    public ResponseEntity<List<SharedOfficeDto>> getAll(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            @RequestParam(required = false) String city
    ) {
        if (city != null && !city.trim().isEmpty()) {
            return ResponseEntity.ok(officeService.getOfficesByCity(city.trim()));
        }
        return ResponseEntity.ok(officeService.getAllOffices(activeOnly));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SharedOfficeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(officeService.getOfficeById(id));
    }

    @PostMapping
    public ResponseEntity<SharedOfficeDto> create(@RequestBody SharedOfficeDto dto) {
        return ResponseEntity.ok(officeService.createOffice(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SharedOfficeDto> update(@PathVariable Long id, @RequestBody SharedOfficeDto dto) {
        return ResponseEntity.ok(officeService.updateOffice(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        officeService.deleteOffice(id);
        return ResponseEntity.noContent().build();
    }
}
