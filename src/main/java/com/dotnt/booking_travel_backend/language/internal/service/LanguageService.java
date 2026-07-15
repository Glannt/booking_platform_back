package com.dotnt.booking_travel_backend.language.internal.service;

import com.dotnt.booking_travel_backend.language.api.CreateLanguageRequest;
import com.dotnt.booking_travel_backend.language.api.LanguageDto;

import java.util.List;

public interface LanguageService {
    LanguageDto createLanguage(CreateLanguageRequest request);
    List<LanguageDto> getActiveLanguages();
    List<LanguageDto> getAllLanguages();
}
