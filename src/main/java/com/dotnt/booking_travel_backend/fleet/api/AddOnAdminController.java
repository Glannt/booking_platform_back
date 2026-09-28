package com.dotnt.booking_travel_backend.fleet.api;

import com.dotnt.booking_travel_backend.fleet.api.dtos.AddOnDTO;
import com.dotnt.booking_travel_backend.fleet.api.dtos.CreateAddOnRequest;
import com.dotnt.booking_travel_backend.fleet.internal.service.IAddOnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addons")
@RequiredArgsConstructor
public class AddOnAdminController {

    private final IAddOnService addOnService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADDON_CREATE')")
    public ResponseEntity<AddOnDTO> create(@RequestBody CreateAddOnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addOnService.createAddOn(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADDON_UPDATE')")
    public ResponseEntity<AddOnDTO> update(@PathVariable Long id, @RequestBody CreateAddOnRequest request) {
        return ResponseEntity.ok(addOnService.updateAddOn(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADDON_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addOnService.deleteAddOn(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddOnDTO> getById(@PathVariable Long id) {
        AddOnDTO dto = addOnService.getAddOnById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/by-category-id/{categoryId}")
    public ResponseEntity<List<AddOnDTO>> getByCategoryId(@PathVariable Long categoryId) {
        return ResponseEntity.ok(addOnService.getAddOnsByCategoryId(categoryId));
    }

    @GetMapping("/by-category-code/{categoryCode}")
    public ResponseEntity<List<AddOnDTO>> getByCategoryCode(@PathVariable String categoryCode) {
        return ResponseEntity.ok(addOnService.getAddOnsByCategoryCode(categoryCode));
    }
}
