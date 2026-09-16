package com.dotnt.booking_travel_backend.fleet.api;

import com.dotnt.booking_travel_backend.fleet.api.dtos.PublicAddOnCategoryDTO;
import com.dotnt.booking_travel_backend.fleet.api.dtos.PublicAddOnDTO;
import com.dotnt.booking_travel_backend.fleet.internal.service.IAddOnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicAddOnController {

    private final IAddOnService addOnService;

    @GetMapping("/addon-categories")
    public ResponseEntity<List<PublicAddOnCategoryDTO>> getCategories(
            @RequestParam(required = false, defaultValue = "vi") String locale,
            @RequestParam(required = false) Boolean filterableOnly) {
        return ResponseEntity.ok(addOnService.getPublicCategories(locale, filterableOnly));
    }

    @GetMapping("/addons")
    public ResponseEntity<List<PublicAddOnDTO>> getAddOnsByCategory(
            @RequestParam String categoryCode,
            @RequestParam(required = false, defaultValue = "vi") String locale) {
        return ResponseEntity.ok(addOnService.getPublicAddOnsByCategory(categoryCode, locale));
    }
}
