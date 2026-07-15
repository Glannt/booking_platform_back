package com.dotnt.booking_travel_backend.language.internal.controller;

import com.dotnt.booking_travel_backend.language.api.CreateLanguageRequest;
import com.dotnt.booking_travel_backend.language.api.LanguageDto;
import com.dotnt.booking_travel_backend.language.internal.service.LanguageService;
import com.dotnt.booking_travel_backend.shared.annotation.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/languages")
@RequiredArgsConstructor
public class LanguageAdminController {

    private final LanguageService languageService;

    @PreAuthorize("hasAuthority('LANGUAGE_CREATE')")
    @PostMapping
    @SuccessResponse("Language created successfully!")
    public ResponseEntity<LanguageDto> createLanguage(@RequestBody CreateLanguageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(languageService.createLanguage(request));
    }

    @GetMapping
    @SuccessResponse("Languages retrieved successfully!")
    public ResponseEntity<List<LanguageDto>> getAllLanguages() {
        return ResponseEntity.ok(languageService.getAllLanguages());
    }
}
