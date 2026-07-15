package com.dotnt.booking_travel_backend.sections.api;

import com.dotnt.booking_travel_backend.sections.internal.service.SectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @GetMapping("/active")
    public ResponseEntity<List<SectionDto>> getActiveSections(
            @RequestParam String pagePath,
            @RequestParam(defaultValue = "vi") String locale) {
        return ResponseEntity.ok(sectionService.getActiveByPagePath(pagePath, locale));
    }

    @GetMapping("/page")
    public ResponseEntity<List<SectionDto>> getByPagePath(@RequestParam String pagePath) {
        return ResponseEntity.ok(sectionService.getAllByPagePath(pagePath));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectionDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sectionService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SECTION_CREATE')")
    public ResponseEntity<SectionDto> create(@Valid @RequestBody CreateSectionRequest request) {
        return ResponseEntity.ok(sectionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SECTION_UPDATE')")
    public ResponseEntity<SectionDto> update(@PathVariable Long id, @Valid @RequestBody CreateSectionRequest request) {
        return ResponseEntity.ok(sectionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SECTION_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sectionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
