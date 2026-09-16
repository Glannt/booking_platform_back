package com.dotnt.booking_travel_backend.selfdrive.api;

import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDriveTypeDto;
import com.dotnt.booking_travel_backend.selfdrive.internal.service.ISelfDriveTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/self-drive-types", "/api/v1/rental-types"})
@RequiredArgsConstructor
public class SelfDriveTypePublicController {
    private final ISelfDriveTypeService typeService;

    @GetMapping
    public ResponseEntity<Page<SelfDriveTypeDto>> list(Pageable pageable) {
        return ResponseEntity.ok(typeService.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SelfDriveTypeDto> get(@PathVariable Long id) {
        SelfDriveTypeDto dto = typeService.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }
}
