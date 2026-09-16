package com.dotnt.booking_travel_backend.selfdrive.api;

import com.dotnt.booking_travel_backend.selfdrive.api.dtos.CategoryAvailabilityDto;
import com.dotnt.booking_travel_backend.shared.annotation.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/self-drive/availability")
@RequiredArgsConstructor
public class SelfDriveAvailabilityController {

    private final SelfDriveFacade selfDriveFacade;

    @GetMapping
    @SuccessResponse("Self-drive availability retrieved successfully")
    public ResponseEntity<List<CategoryAvailabilityDto>> getAvailability(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "vi") String locale
    ) {
        LocalDate start = startDate != null ? startDate : LocalDate.now();
        LocalDate end = endDate != null ? endDate : start;

        if (categoryId != null) {
            CategoryAvailabilityDto single = selfDriveFacade.checkSelfDriveAvailability(categoryId, start, end, locale);
            return ResponseEntity.ok(List.of(single));
        }

        List<CategoryAvailabilityDto> all = selfDriveFacade.getSelfDriveAvailabilityForAll(start, end, locale);
        return ResponseEntity.ok(all);
    }
}
