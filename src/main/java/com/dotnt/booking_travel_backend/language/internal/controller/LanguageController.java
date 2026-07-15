package com.dotnt.booking_travel_backend.language.internal.controller;

import com.dotnt.booking_travel_backend.language.api.LanguageDto;
import com.dotnt.booking_travel_backend.language.internal.service.LanguageService;
import com.dotnt.booking_travel_backend.shared.annotation.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/languages")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageService languageService;

    @GetMapping
    @SuccessResponse("Active languages retrieved successfully!")
    public ResponseEntity<List<LanguageDto>> getActiveLanguages() {
        return ResponseEntity.ok(languageService.getActiveLanguages());
    }
}
