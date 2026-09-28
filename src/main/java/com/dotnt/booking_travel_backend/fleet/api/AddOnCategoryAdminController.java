package com.dotnt.booking_travel_backend.fleet.api;

import com.dotnt.booking_travel_backend.fleet.api.dtos.AddOnCategoryDTO;
import com.dotnt.booking_travel_backend.fleet.api.dtos.CreateAddOnCategoryRequest;
import com.dotnt.booking_travel_backend.fleet.internal.service.IAddOnService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addon-categories")
@RequiredArgsConstructor
public class AddOnCategoryAdminController {

    private final IAddOnService addOnService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADDON_CATEGORY_CREATE')")
    public ResponseEntity<AddOnCategoryDTO> create(@RequestBody CreateAddOnCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addOnService.createCategory(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADDON_CATEGORY_UPDATE')")
    public ResponseEntity<AddOnCategoryDTO> update(@PathVariable Long id, @RequestBody CreateAddOnCategoryRequest request) {
        return ResponseEntity.ok(addOnService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADDON_CATEGORY_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addOnService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddOnCategoryDTO> getById(@PathVariable Long id) {
        AddOnCategoryDTO dto = addOnService.getCategoryById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<AddOnCategoryDTO>> getAll(@RequestParam(required = false) Boolean activeOnly) {
        return ResponseEntity.ok(addOnService.getAllCategories(activeOnly));
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<AddOnCategoryDTO>> getPaged(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(addOnService.getCategoriesPaged(search, pageable));
    }
}
